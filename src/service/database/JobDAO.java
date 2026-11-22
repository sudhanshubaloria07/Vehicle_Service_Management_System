package service.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import service.core.ServiceJob;
import service.exception.InvalidJobException;

/**
 * Data Access Object (DAO) for managing service appointments and job cards.
 * 
 * OOP CONCEPT: Encapsulation & Separation of Concerns (Unit 1)
 * OOP CONCEPT: Exception Handling (Unit 3) - Throws custom InvalidJobException
 * OOP CONCEPT: Transaction Management (Unit 3) - Atomic Job ID generation & insertion
 */
public class JobDAO {

    /**
     * Generates a unique, formatted Job ID (JOB-YYYY-NNNN) and inserts the job
     * within a single atomic database transaction.
     * 
     * @param job ServiceJob instance with vehicle ID, service type, date, labour
     * @return Generated Job ID
     * @throws SQLException If database insertion fails
     */
    public String createJob(ServiceJob job) throws SQLException {
        Connection conn = null;
        PreparedStatement psLock = null;
        PreparedStatement psInsert = null;
        ResultSet rs = null;

        int currentYear = LocalDate.now().getYear();
        String yearPrefix = "JOB-" + currentYear + "-";
        String generatedJobId = null;

        try {
            conn = DBConnection.getConnection();
            // Start transaction
            conn.setAutoCommit(false);

            // 1. Query highest sequence for the current year
            String lockQuery = "SELECT MAX(job_id) FROM Jobs WHERE job_id LIKE ?";
            psLock = conn.prepareStatement(lockQuery);
            psLock.setString(1, yearPrefix + "%");
            rs = psLock.executeQuery();

            int nextSeq = 1;
            if (rs.next()) {
                String maxId = rs.getString(1);
                if (maxId != null && maxId.startsWith(yearPrefix)) {
                    try {
                        String seqPart = maxId.substring(yearPrefix.length());
                        nextSeq = Integer.parseInt(seqPart) + 1;
                    } catch (NumberFormatException ignored) {
                        nextSeq = 1;
                    }
                }
            }

            // Format as JOB-2026-0001
            generatedJobId = String.format("%s%04d", yearPrefix, nextSeq);

            // 2. Insert new ServiceJob record
            String insertSql = "INSERT INTO Jobs (job_id, vehicle_id, service_type, booking_date, job_status, labour_charges) "
                             + "VALUES (?, ?, ?, ?, ?, ?)";
            psInsert = conn.prepareStatement(insertSql);
            psInsert.setString(1, generatedJobId);
            psInsert.setInt(2, job.getVehicleId());
            psInsert.setString(3, job.getServiceType());
            psInsert.setDate(4, java.sql.Date.valueOf(job.getBookingDate()));
            psInsert.setString(5, (job.getJobStatus() != null) ? job.getJobStatus() : "Pending");
            psInsert.setDouble(6, job.getLabourCharges());

            psInsert.executeUpdate();

            // Commit atomic transaction
            conn.commit();
            job.setJobId(generatedJobId);
            return generatedJobId;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on any failure
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            // Restore auto-commit and close statements
            if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
            if (psLock != null) try { psLock.close(); } catch (SQLException ignored) {}
            if (psInsert != null) try { psInsert.close(); } catch (SQLException ignored) {}
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Retrieves a single job by Job ID, joined with Vehicle and Customer tables.
     * 
     * @param jobId Readable Job ID (e.g. JOB-2026-0001)
     * @return Fully populated ServiceJob instance
     * @throws InvalidJobException If the Job ID does not exist or is malformed
     * @throws SQLException If database query fails
     */
    public ServiceJob getJobById(String jobId) throws InvalidJobException, SQLException {
        if (jobId == null || jobId.trim().isEmpty()) {
            throw new InvalidJobException("Job ID cannot be blank.");
        }

        String sql = "SELECT j.job_id, j.vehicle_id, j.service_type, j.booking_date, j.job_status, j.labour_charges, "
                   + "       v.registration_number, v.brand, v.model, v.vehicle_type, v.customer_name, v.phone "
                   + "FROM Jobs j "
                   + "JOIN Vehicle v ON j.vehicle_id = v.vehicle_id "
                   + "WHERE UPPER(j.job_id) = UPPER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, jobId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String vDetails = "[" + rs.getString("vehicle_type") + "] " + rs.getString("brand") + " " + rs.getString("model");
                    return new ServiceJob(
                        rs.getString("job_id"),
                        rs.getInt("vehicle_id"),
                        rs.getString("service_type"),
                        rs.getString("booking_date"),
                        rs.getString("job_status"),
                        rs.getDouble("labour_charges"),
                        vDetails,
                        rs.getString("registration_number"),
                        rs.getString("customer_name"),
                        rs.getString("phone")
                    );
                } else {
                    // OOP CONCEPT: Custom Exception thrown when record not found
                    throw new InvalidJobException("No service job found with ID: " + jobId);
                }
            }
        }
    }

    /**
     * Updates status of a service job with business transition validation.
     * 
     * @param jobId     Job ID
     * @param newStatus "Pending", "In Progress", or "Completed"
     * @return true if updated
     * @throws InvalidJobException If invalid status transition is attempted
     */
    public boolean updateJobStatus(String jobId, String newStatus) throws InvalidJobException, SQLException {
        ServiceJob current = getJobById(jobId);

        // Transition rule: Completed jobs are final; cannot move back to Pending or In Progress
        if ("Completed".equalsIgnoreCase(current.getJobStatus()) && !"Completed".equalsIgnoreCase(newStatus)) {
            throw new InvalidJobException("Illegal state transition: Job " + jobId + " is already 'Completed' and cannot be reopened.");
        }

        String sql = "UPDATE Jobs SET job_status = ? WHERE job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setString(2, jobId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all jobs ordered by booking date and ID descending.
     */
    public List<ServiceJob> getAllJobs() throws SQLException {
        List<ServiceJob> list = new ArrayList<>();
        String sql = "SELECT j.job_id, j.vehicle_id, j.service_type, j.booking_date, j.job_status, j.labour_charges, "
                   + "       v.registration_number, v.brand, v.model, v.vehicle_type, v.customer_name, v.phone "
                   + "FROM Jobs j "
                   + "JOIN Vehicle v ON j.vehicle_id = v.vehicle_id "
                   + "ORDER BY j.booking_date DESC, j.job_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String vDetails = "[" + rs.getString("vehicle_type") + "] " + rs.getString("brand") + " " + rs.getString("model");
                ServiceJob job = new ServiceJob(
                    rs.getString("job_id"),
                    rs.getInt("vehicle_id"),
                    rs.getString("service_type"),
                    rs.getString("booking_date"),
                    rs.getString("job_status"),
                    rs.getDouble("labour_charges"),
                    vDetails,
                    rs.getString("registration_number"),
                    rs.getString("customer_name"),
                    rs.getString("phone")
                );
                list.add(job);
            }
        }
        return list;
    }

    /**
     * Retrieves the N most recent service jobs.
     */
    public List<ServiceJob> getRecentJobs(int limit) throws SQLException {
        List<ServiceJob> list = new ArrayList<>();
        String sql = "SELECT j.job_id, j.vehicle_id, j.service_type, j.booking_date, j.job_status, j.labour_charges, "
                   + "       v.registration_number, v.brand, v.model, v.vehicle_type, v.customer_name, v.phone "
                   + "FROM Jobs j "
                   + "JOIN Vehicle v ON j.vehicle_id = v.vehicle_id "
                   + "ORDER BY j.booking_date DESC, j.job_id DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String vDetails = "[" + rs.getString("vehicle_type") + "] " + rs.getString("brand") + " " + rs.getString("model");
                    ServiceJob job = new ServiceJob(
                        rs.getString("job_id"),
                        rs.getInt("vehicle_id"),
                        rs.getString("service_type"),
                        rs.getString("booking_date"),
                        rs.getString("job_status"),
                        rs.getDouble("labour_charges"),
                        vDetails,
                        rs.getString("registration_number"),
                        rs.getString("customer_name"),
                        rs.getString("phone")
                    );
                    list.add(job);
                }
            }
        }
        return list;
    }
}
