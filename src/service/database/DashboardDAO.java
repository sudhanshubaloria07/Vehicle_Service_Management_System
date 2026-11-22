package service.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import service.core.ServiceJob;

/**
 * Data Access Object (DAO) for Dashboard statistics and aggregated metrics.
 * 
 * OOP CONCEPT: Encapsulation & Separation of Concerns (Unit 1 & 2)
 */
public class DashboardDAO {

    private final JobDAO jobDAO;

    public DashboardDAO() {
        this.jobDAO = new JobDAO();
    }

    /**
     * Counts total registered vehicles.
     */
    public int getTotalVehicles() throws SQLException {
        return executeCountQuery("SELECT COUNT(*) FROM Vehicle");
    }

    /**
     * Counts total service jobs booked.
     */
    public int getTotalBookings() throws SQLException {
        return executeCountQuery("SELECT COUNT(*) FROM Jobs");
    }

    /**
     * Counts jobs currently in 'Pending' status.
     */
    public int getPendingJobsCount() throws SQLException {
        return executeCountQuery("SELECT COUNT(*) FROM Jobs WHERE job_status = 'Pending'");
    }

    /**
     * Counts jobs currently 'In Progress'.
     */
    public int getInProgressJobsCount() throws SQLException {
        return executeCountQuery("SELECT COUNT(*) FROM Jobs WHERE job_status = 'In Progress'");
    }

    /**
     * Counts jobs that have reached 'Completed' status.
     */
    public int getCompletedJobsCount() throws SQLException {
        return executeCountQuery("SELECT COUNT(*) FROM Jobs WHERE job_status = 'Completed'");
    }

    /**
     * Counts parts with stock quantity below critical threshold (< 5).
     */
    public int getLowStockPartsCount() throws SQLException {
        return executeCountQuery("SELECT COUNT(*) FROM Parts WHERE quantity < 5");
    }

    /**
     * Retrieves the latest service jobs for dashboard overview.
     */
    public List<ServiceJob> getRecentJobs(int limit) throws SQLException {
        return jobDAO.getRecentJobs(limit);
    }

    private int executeCountQuery(String sql) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
