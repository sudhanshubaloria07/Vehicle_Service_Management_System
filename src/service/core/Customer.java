package service.core;

/**
 * Customer model class representing vehicle owners.
 * 
 * OOP CONCEPT: Class & Object (Unit 1)
 * OOP CONCEPT: Encapsulation (Unit 1) - Private fields accessed via public getters/setters
 * OOP CONCEPT: Composition (Unit 2) - Customer is contained in Vehicle (HAS-A relationship)
 */
public class Customer {

    // OOP CONCEPT: Encapsulation - private attributes
    private String name;
    private String phone;

    /**
     * OOP CONCEPT: Constructor Overloading - Default Constructor
     */
    public Customer() {
        this.name = "";
        this.phone = "";
    }

    /**
     * OOP CONCEPT: Constructor Overloading - Parameterized Constructor
     * 
     * @param name  Full customer name
     * @param phone 10-digit mobile contact number
     */
    public Customer(String name, String phone) {
        setName(name);
        setPhone(phone);
    }

    // OOP CONCEPT: Encapsulation - Getters and Setters with validation
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = (name != null) ? name.trim() : "";
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = (phone != null) ? phone.trim() : "";
    }

    @Override
    public String toString() {
        return name + " (" + phone + ")";
    }
}
