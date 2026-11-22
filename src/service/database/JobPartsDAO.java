package service.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import service.billing.Bill;
import service.exception.InvalidJobException;

/**
 * Data Access Object (DAO) for managing the junction table Job_Parts.
 * Enforces atomic database transactions with row-level locking (SELECT ... FOR UPDATE).
 * 
 * OOP CONCEPT: Transaction Management & Atomicity (Unit 3)
 * OOP CONCEPT: Exception Handling (Unit 3) - Custom InvalidJobException & SQLExceptions
 * OOP CONCEPT: Math API (Unit 1) - Stock validation and rounding
 */
public class JobPartsDAO {

    /**
     * Assigns a spare part to a service job within an ACID transaction.
     * 1. Validates job status (cannot add parts to a Completed job).
     * 2. Locks the part row with SELECT ... FOR UPDATE.
     * 3. Checks available stock vs requested quantity.
     * 4. Updates or inserts Job_Parts record.
     * 5. Decrements stock in Parts table.
     * 6. Commits transaction or rolls back on any error.
     * 
     * @param jobId    Valid service job ID
     * @param partId   Spare part ID
     * @param qtyUsed  Quantity to allocate (must be > 0)
     * @throws InvalidJobException If job is completed or invalid
     * @throws SQLException        If insufficient stock or database error occurs
     */
    public void assignPartToJob(String jobId, int partId, int qtyUsed) throws InvalidJobException, SQLException {
        if (qtyUsed <= 0) {
            throw new IllegalArgumentException("Quantity to assign must be greater than 0.");
        }

        Connection conn = null;
        PreparedStatement psCheckJob = null;
        PreparedStatement psLockPart = null;
        PreparedStatement psCheckJobPart = null;
        PreparedStatement psUpdateJobPart = null;
        PreparedStatement psInsertJobPart = null;
        PreparedStatement psDeductPart = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            // OOP CONCEPT: Transaction Management - Disable auto-commit for atomic batch operations
            conn.setAutoCommit(false);

            // 1. Verify job is not Completed
            String jobCheckSql = "SELECT job_status FROM Jobs WHERE job_id = ?";
            psCheckJob = conn.prepareStatement(jobCheckSql);
            psCheckJob.setString(1, jobId);
            rs = psCheckJob.executeQuery();
            if (rs.next()) {
                String status = rs.getString("job_status");
                if ("Completed".equalsIgnoreCase(status)) {
                    throw new InvalidJobException("Cannot assign parts: Job " + jobId + " is already marked as 'Completed'.");
                }
            } else {
                throw new InvalidJobException("Job ID " + jobId + " does not exist.");
            }
            rs.close();

            // 2. Lock and read part stock using row-level lock
            String lockPartSql = "SELECT part_name, quantity, price FROM Parts WHERE part_id = ? FOR UPDATE";
            psLockPart = conn.prepareStatement(lockPartSql);
            psLockPart.setInt(1, partId);
            rs = psLockPart.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Part with ID " + partId + " was not found in inventory.");
            }

            String partName = rs.getString("part_name");
            int availableStock = rs.getInt("quantity");
            rs.close();

            // Stock check
            if (availableStock < qtyUsed) {
                // Insufficient stock: abort transaction
                throw new SQLException("Insufficient stock for '" + partName + "'. Available: " 
                        + availableStock + ", requested: " + qtyUsed);
            }

            // 3. Check if this part is already assigned to this job
            String checkJobPartSql = "SELECT quantity_used FROM Job_Parts WHERE job_id = ? AND part_id = ?";
            psCheckJobPart = conn.prepareStatement(checkJobPartSql);
            psCheckJobPart.setString(1, jobId);
            psCheckJobPart.setInt(2, partId);
            rs = psCheckJobPart.executeQuery();

            if (rs.next()) {
                // Part already present: update and increment quantity
                String updateJpSql = "UPDATE Job_Parts SET quantity_used = quantity_used + ? WHERE job_id = ? AND part_id = ?";
                psUpdateJobPart = conn.prepareStatement(updateJpSql);
                psUpdateJobPart.setInt(1, qtyUsed);
                psUpdateJobPart.setString(2, jobId);
                psUpdateJobPart.setInt(3, partId);
                psUpdateJobPart.executeUpdate();
            } else {
                // Part not yet linked: insert new row into junction table
                String insertJpSql = "INSERT INTO Job_Parts (job_id, part_id, quantity_used) VALUES (?, ?, ?)";
                psInsertJobPart = conn.prepareStatement(insertJpSql);
                psInsertJobPart.setString(1, jobId);
                psInsertJobPart.setInt(2, partId);
                psInsertJobPart.setInt(3, qtyUsed);
                psInsertJobPart.executeUpdate();
            }
            rs.close();

            // 4. Deduct allocated quantity from inventory
            // OOP CONCEPT: Math API - Math.max ensures stock never drops below 0
            int updatedStock = Math.max(0, availableStock - qtyUsed);
            String deductSql = "UPDATE Parts SET quantity = ? WHERE part_id = ?";
            psDeductPart = conn.prepareStatement(deductSql);
            psDeductPart.setInt(1, updatedStock);
            psDeductPart.setInt(2, partId);
            psDeductPart.executeUpdate();

            // Commit atomic transaction
            conn.commit();

        } catch (SQLException | InvalidJobException ex) {
            // Roll back all changes if any query failed
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw ex;
        } finally {
            // OOP CONCEPT: Exception Handling (try/finally) - Always restore auto-commit and release resources
            if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
            if (psCheckJob != null) try { psCheckJob.close(); } catch (SQLException ignored) {}
            if (psLockPart != null) try { psLockPart.close(); } catch (SQLException ignored) {}
            if (psCheckJobPart != null) try { psCheckJobPart.close(); } catch (SQLException ignored) {}
            if (psUpdateJobPart != null) try { psUpdateJobPart.close(); } catch (SQLException ignored) {}
            if (psInsertJobPart != null) try { psInsertJobPart.close(); } catch (SQLException ignored) {}
            if (psDeductPart != null) try { psDeductPart.close(); } catch (SQLException ignored) {}
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Retrieves all parts assigned to a specific job with unit prices and calculated line totals.
     * 
     * @param jobId Service Job ID
     * @return List of LineItem instances
     */
    public List<Bill.LineItem> getPartsForJob(String jobId) throws SQLException {
        List<Bill.LineItem> list = new ArrayList<>();
        String sql = "SELECT p.part_name, p.price, jp.quantity_used "
                   + "FROM Job_Parts jp "
                   + "JOIN Parts p ON jp.part_id = p.part_id "
                   + "WHERE jp.job_id = ? "
                   + "ORDER BY p.part_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Bill.LineItem(
                        rs.getString("part_name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity_used")
                    ));
                }
            }
        }
        return list;
    }
}
