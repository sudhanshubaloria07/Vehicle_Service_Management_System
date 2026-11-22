package service.core;

/**
 * Model class representing a service job appointment.
 * 
 * OOP CONCEPT: Class & Object (Unit 1)
 * OOP CONCEPT: Encapsulation (Unit 1) - Protected state with public accessors
 */
public class ServiceJob {

    private String jobId;
    private int vehicleId;
    private String serviceType;
    private String bookingDate; // YYYY-MM-DD
    private String jobStatus;    // "Pending", "In Progress", "Completed"
    private double labourCharges;

    // Additional helper fields for display conveniences in tables/cards
    private String vehicleDetails;
    private String registrationNumber;
    private String customerName;
    private String customerPhone;

    /**
     * OOP CONCEPT: Constructor Overloading - Default Constructor
     */
    public ServiceJob() {
        this.jobId = "";
        this.vehicleId = 0;
        this.serviceType = "General Service";
        this.bookingDate = "";
        this.jobStatus = "Pending";
        this.labourCharges = 0.0;
    }

    /**
     * OOP CONCEPT: Constructor Overloading - Core Parameterized Constructor
     */
    public ServiceJob(String jobId, int vehicleId, String serviceType, String bookingDate, String jobStatus, double labourCharges) {
        this.jobId = jobId;
        this.vehicleId = vehicleId;
        this.serviceType = serviceType;
        this.bookingDate = bookingDate;
        this.jobStatus = jobStatus;
        setLabourCharges(labourCharges);
    }

    /**
     * OOP CONCEPT: Constructor Overloading - Full Display Constructor
     */
    public ServiceJob(String jobId, int vehicleId, String serviceType, String bookingDate, String jobStatus, 
                      double labourCharges, String vehicleDetails, String registrationNumber, 
                      String customerName, String customerPhone) {
        this(jobId, vehicleId, serviceType, bookingDate, jobStatus, labourCharges);
        this.vehicleDetails = vehicleDetails;
        this.registrationNumber = registrationNumber;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
    }

    // Encapsulated getters and setters
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = (jobId != null) ? jobId.trim() : "";
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public double getLabourCharges() {
        return labourCharges;
    }

    public void setLabourCharges(double labourCharges) {
        // OOP CONCEPT: Math API - Rounding to 2 decimal places
        this.labourCharges = Math.round(labourCharges * 100.0) / 100.0;
    }

    public String getVehicleDetails() {
        return vehicleDetails;
    }

    public void setVehicleDetails(String vehicleDetails) {
        this.vehicleDetails = vehicleDetails;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    @Override
    public String toString() {
        return jobId + " [" + serviceType + " - " + jobStatus + "]";
    }
}
