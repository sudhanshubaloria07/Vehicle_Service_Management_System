package service.core;

import java.sql.SQLException;
import java.util.List;
import service.billing.Bill;
import service.database.JobDAO;
import service.database.JobPartsDAO;
import service.database.VehicleDAO;
import service.exception.InvalidJobException;
import service.vehicle.Vehicle;

/**
 * Service orchestrator for generating comprehensive invoices for completed jobs.
 * 
 * OOP CONCEPT: Interface Usage & Polymorphism (Unit 2)
 * OOP CONCEPT: Exception Handling (Unit 3) - Enforces billing only for Completed jobs
 */
public class BillingService {

    private final JobDAO jobDAO;
    private final VehicleDAO vehicleDAO;
    private final JobPartsDAO jobPartsDAO;

    public BillingService() {
        this.jobDAO = new JobDAO();
        this.vehicleDAO = new VehicleDAO();
        this.jobPartsDAO = new JobPartsDAO();
    }

    /**
     * Generates a fully populated Bill instance for a completed service job.
     * 
     * @param jobId Service Job ID (e.g. JOB-2026-0001)
     * @return Complete Bill instance ready for printing or preview
     * @throws InvalidJobException If the job is not in 'Completed' status or doesn't exist
     * @throws SQLException        If a database query error occurs
     */
    public Bill generateBill(String jobId) throws InvalidJobException, SQLException {
        if (jobId == null || jobId.trim().isEmpty()) {
            throw new InvalidJobException("Job ID is required for generating a bill.");
        }

        // 1. Fetch job details
        ServiceJob job = jobDAO.getJobById(jobId.trim());
        if (job == null) {
            throw new InvalidJobException("Job not found: " + jobId);
        }

        // Rule: Only Completed jobs can be billed
        if (!"Completed".equalsIgnoreCase(job.getJobStatus())) {
            throw new InvalidJobException("Cannot generate bill: Job " + jobId 
                    + " is currently '" + job.getJobStatus() 
                    + "'. Only 'Completed' jobs can be billed.");
        }

        // 2. Fetch polymorphic Vehicle
        Vehicle vehicle = vehicleDAO.getVehicleById(job.getVehicleId());

        // 3. Create Bill instance
        Bill bill = new Bill(job, vehicle);

        // 4. Fetch actual parts assigned to this job
        List<Bill.LineItem> partsUsed = jobPartsDAO.getPartsForJob(jobId);
        for (Bill.LineItem item : partsUsed) {
            bill.addLineItem(item.getPartName(), item.getUnitPrice(), item.getQuantity());
        }

        // 5. Trigger calculation across labour, parts, and GST
        bill.calculateBill();
        bill.calculateGST();
        bill.generateFinalAmount();

        return bill;
    }
}
