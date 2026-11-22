package service.vehicle;

import service.core.Customer;

/**
 * Concrete Bike subclass representing two-wheelers and motorcycles.
 * 
 * OOP CONCEPT: Inheritance (Unit 2) - Bike extends Vehicle
 * OOP CONCEPT: Method Overriding (Unit 2) - Overrides getVehicleDetails() and getLabourMultiplier()
 * OOP CONCEPT: Runtime Polymorphism (Unit 2) - Standard baseline multiplier (1.0x)
 */
public class Bike extends Vehicle {

    /**
     * OOP CONCEPT: Constructor Overloading - Default Constructor
     */
    public Bike() {
        super();
        setVehicleType("Bike");
    }

    /**
     * OOP CONCEPT: Constructor Overloading - Parameterized Constructor
     * Uses super() to invoke the base Vehicle constructor.
     * 
     * @param vehicleId          Database ID
     * @param customer           Owner customer
     * @param registrationNumber Indian registration plate string
     * @param brand              Motorcycle maker (e.g. Royal Enfield, Honda, TVS)
     * @param model              Motorcycle model (e.g. Classic 350, Activa 6G)
     */
    public Bike(int vehicleId, Customer customer, String registrationNumber, String brand, String model) {
        super(vehicleId, customer, registrationNumber, brand, model, "Bike");
    }

    /**
     * OOP CONCEPT: Method Overriding (Unit 2)
     * Custom details formatted specifically for Two-Wheelers.
     */
    @Override
    public String getVehicleDetails() {
        return "[Two-Wheeler/Bike] " + getBrand() + " " + getModel() 
                + " | Reg: " + getRegistrationNumber() 
                + " | Owner: " + getCustomer().getName();
    }

    /**
     * OOP CONCEPT: Method Overriding & Runtime Polymorphism (Unit 2)
     * Two-wheelers use the standard baseline multiplier of 1.0x.
     * 
     * @return 1.0 labour multiplier
     */
    @Override
    public double getLabourMultiplier() {
        return 1.0;
    }
}
