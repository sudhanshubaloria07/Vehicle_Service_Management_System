package service.vehicle;

import service.core.Customer;

/**
 * Concrete Car subclass representing four-wheeled passenger vehicles.
 * 
 * OOP CONCEPT: Inheritance (Unit 2) - Car extends Vehicle
 * OOP CONCEPT: Method Overriding (Unit 2) - Overrides getVehicleDetails() and getLabourMultiplier()
 * OOP CONCEPT: Runtime Polymorphism (Unit 2) - Higher labour multiplier (1.5x) for complex mechanics
 */
public class Car extends Vehicle {

    /**
     * OOP CONCEPT: Constructor Overloading - Default Constructor
     */
    public Car() {
        super();
        setVehicleType("Car");
    }

    /**
     * OOP CONCEPT: Constructor Overloading - Parameterized Constructor
     * Uses super() to invoke the base Vehicle constructor.
     * 
     * @param vehicleId          Database ID
     * @param customer           Owner customer
     * @param registrationNumber Indian registration plate string
     * @param brand              Car maker (e.g. Hyundai, Tata, Maruti)
     * @param model              Car model (e.g. i20, Nexon, Swift)
     */
    public Car(int vehicleId, Customer customer, String registrationNumber, String brand, String model) {
        super(vehicleId, customer, registrationNumber, brand, model, "Car");
    }

    /**
     * OOP CONCEPT: Method Overriding (Unit 2)
     * Custom details formatted specifically for Car.
     */
    @Override
    public String getVehicleDetails() {
        return "[Car] " + getBrand() + " " + getModel() 
                + " | Reg: " + getRegistrationNumber() 
                + " | Owner: " + getCustomer().getName();
    }

    /**
     * OOP CONCEPT: Method Overriding & Runtime Polymorphism (Unit 2)
     * Four-wheelers require more labour and equipment, hence a 1.5x multiplier.
     * 
     * @return 1.5 labour multiplier
     */
    @Override
    public double getLabourMultiplier() {
        return 1.5;
    }
}
