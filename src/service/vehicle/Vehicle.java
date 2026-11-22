package service.vehicle;

import service.core.Customer;

/**
 * Abstract Vehicle base class representing the common properties and behaviors
 * of all vehicles serviced in the system.
 * 
 * OOP CONCEPT: Inheritance (Unit 2) - Base parent class for Car and Bike
 * OOP CONCEPT: Composition (Unit 2) - Vehicle HAS-A Customer object
 * OOP CONCEPT: Encapsulation (Unit 1) - Protected/private fields with public accessors
 */
public abstract class Vehicle {

    // Encapsulated vehicle attributes
    private int vehicleId;
    private Customer customer; // OOP CONCEPT: Composition (HAS-A relationship)
    private String registrationNumber;
    private String brand;
    private String model;
    private String vehicleType;

    /**
     * OOP CONCEPT: Constructor Overloading - Default Constructor
     */
    public Vehicle() {
        this.vehicleId = 0;
        this.customer = new Customer();
        this.registrationNumber = "";
        this.brand = "";
        this.model = "";
        this.vehicleType = "";
    }

    /**
     * OOP CONCEPT: Constructor Overloading - Parameterized Constructor
     * 
     * @param vehicleId          Database primary key
     * @param customer           Customer owner instance (Composition)
     * @param registrationNumber Indian vehicle registration string
     * @param brand              Manufacturer brand (e.g., Hyundai, Tata, Honda)
     * @param model              Model name (e.g., i20, Nexon, Activa)
     * @param vehicleType        Category: "Car" or "Bike"
     */
    public Vehicle(int vehicleId, Customer customer, String registrationNumber, String brand, String model, String vehicleType) {
        this.vehicleId = vehicleId;
        this.customer = (customer != null) ? customer : new Customer();
        setRegistrationNumber(registrationNumber);
        setBrand(brand);
        setModel(model);
        this.vehicleType = vehicleType;
    }

    // OOP CONCEPT: Encapsulation - Accessors and Mutators
    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = (customer != null) ? customer : new Customer();
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = (registrationNumber != null) ? registrationNumber.trim().toUpperCase() : "";
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = (brand != null) ? brand.trim() : "";
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = (model != null) ? model.trim() : "";
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    /**
     * Abstract method for polymorphic vehicle description.
     * OOP CONCEPT: Runtime Polymorphism & Method Overriding (Unit 2)
     * 
     * @return Formatted vehicle details
     */
    public abstract String getVehicleDetails();

    /**
     * Abstract method returning the labour surcharge multiplier.
     * OOP CONCEPT: Runtime Polymorphism (Unit 2)
     * 
     * @return Multiplier (e.g., 1.5 for Car, 1.0 for Bike)
     */
    public abstract double getLabourMultiplier();

    /**
     * Calculates the polymorphic labour charge for a given base service rate.
     * Demonstrates dynamic method dispatch via getLabourMultiplier().
     * 
     * @param baseLabour Base charge for the service category
     * @return Final labour charge adjusted for vehicle complexity
     */
    public double calculateLabourSurcharge(double baseLabour) {
        // OOP CONCEPT: Runtime Polymorphism - getLabourMultiplier() dynamically resolved
        double multiplier = getLabourMultiplier();
        double total = baseLabour * multiplier;
        // OOP CONCEPT: Math API - Rounding to 2 decimal places
        return Math.round(total * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return registrationNumber + " - " + brand + " " + model + " (" + customer.getName() + ")";
    }
}
