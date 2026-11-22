package service.core;

import java.sql.SQLException;
import java.util.List;
import service.database.JobDAO;
import service.database.VehicleDAO;
import service.exception.InvalidJobException;
import service.vehicle.Vehicle;

/**
 * Service orchestrator for booking and managing vehicle service jobs.
 * 
 * OOP CONCEPT: Runtime Polymorphism (Unit 2) - Calculates labour charges via Vehicle.calculateLabourSurcharge()
 * OOP CONCEPT: Separation of Concerns - Coordinates between GUI and DAOs
 */
public class JobService {

    // Base rates for service categories
    public static final double BASE_GENERAL_SERVICE = 1200.00;
    public static final double BASE_ENGINE_REPAIR   = 3500.00;
    public static final double BASE_OIL_CHANGE      = 500.00;
    public static final double BASE_BRAKE_INSPECTION= 800.00;

    private final JobDAO jobDAO;
    private final VehicleDAO vehicleDAO;

    public JobService() {
        this.jobDAO = new JobDAO();
        this.vehicleDAO = new VehicleDAO();
    }

    /**
     * Returns the base labour rate for a given service type name.
     */
    public static double getBaseLabourRate(String serviceType) {
        if (serviceType == null) return BASE_GENERAL_SERVICE;
        switch (serviceType) {
            case "Engine Repair":
                return BASE_ENGINE_REPAIR;
            case "Oil Change":
                return BASE_OIL_CHANGE;
            case "Brake Inspection":
                return BASE_BRAKE_INSPECTION;
            case "General Service":
            default:
                return BASE_GENERAL_SERVICE;
        }
    }

    /**
     * Calculates labour charge polymorphically using the vehicle instance.
     * 
     * OOP CONCEPT: Runtime Polymorphism (Unit 2)
     * Calls vehicle.calculateLabourSurcharge(baseRate) which executes the Car or Bike implementation
     * of getLabourMultiplier() dynamically at runtime.
     * 
     * @param vehicle     Polymorphic Vehicle reference (Car or Bike)
     * @param serviceType Service category name
     * @return Computed labour charge in INR
     */
    public double calculateLabourCharge(Vehicle vehicle, String serviceType) {
        if (vehicle == null) {
            return 0.0;
        }
        double baseRate = getBaseLabourRate(serviceType);
        // OOP CONCEPT: Polymorphic method dispatch
        return vehicle.calculateLabourSurcharge(baseRate);
    }

    /**
     * Books a new service job for a vehicle.
     * 
     * @param vehicleId   ID of the registered vehicle
     * @param serviceType Service category
     * @param bookingDate Booking date string (YYYY-MM-DD)
     * @return Generated Job ID
     */
    public String bookService(int vehicleId, String serviceType, String bookingDate) throws SQLException, InvalidJobException {
        Validator.validateBookingDate(bookingDate);

        Vehicle vehicle = vehicleDAO.getVehicleById(vehicleId);
        if (vehicle == null) {
            throw new InvalidJobException("Selected vehicle (ID: " + vehicleId + ") was not found in the database.");
        }

        // Calculate labour charge polymorphically
        double labour = calculateLabourCharge(vehicle, serviceType);

        ServiceJob job = new ServiceJob();
        job.setVehicleId(vehicleId);
        job.setServiceType(serviceType);
        job.setBookingDate(bookingDate);
        job.setJobStatus("Pending");
        job.setLabourCharges(labour);

        return jobDAO.createJob(job);
    }

    /**
     * Retrieves single job details.
     */
    public ServiceJob getJobDetails(String jobId) throws InvalidJobException, SQLException {
        return jobDAO.getJobById(jobId);
    }

    /**
     * Updates status of an existing job.
     */
    public boolean updateJobStatus(String jobId, String newStatus) throws InvalidJobException, SQLException {
        return jobDAO.updateJobStatus(jobId, newStatus);
    }

    /**
     * Retrieves all jobs.
     */
    public List<ServiceJob> getAllJobs() throws SQLException {
        return jobDAO.getAllJobs();
    }

    /**
     * Retrieves recent jobs for quick view.
     */
    public List<ServiceJob> getRecentJobs(int limit) throws SQLException {
        return jobDAO.getRecentJobs(limit);
    }
}
