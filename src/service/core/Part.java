package service.core;

/**
 * Part model class representing inventory items.
 * 
 * OOP CONCEPT: Class & Object (Unit 1)
 * OOP CONCEPT: Encapsulation (Unit 1) - Guarded private fields with validation
 */
public class Part {

    // OOP CONCEPT: Encapsulation - private attributes
    private int partId;
    private String partName;
    private double price;
    private int quantity;

    /**
     * OOP CONCEPT: Constructor Overloading - Default Constructor
     */
    public Part() {
        this.partId = 0;
        this.partName = "";
        this.price = 0.0;
        this.quantity = 0;
    }

    /**
     * OOP CONCEPT: Constructor Overloading - Parameterized Constructor (without ID for creation)
     * 
     * @param partName Name/description of the spare part
     * @param price    Unit price in INR (non-negative)
     * @param quantity Available stock quantity (non-negative)
     */
    public Part(String partName, double price, int quantity) {
        this(0, partName, price, quantity);
    }

    /**
     * OOP CONCEPT: Constructor Overloading - Full Parameterized Constructor
     * 
     * @param partId   Database primary key ID
     * @param partName Name/description of the spare part
     * @param price    Unit price in INR (non-negative)
     * @param quantity Available stock quantity (non-negative)
     */
    public Part(int partId, String partName, double price, int quantity) {
        this.partId = partId;
        setPartName(partName);
        setPrice(price);
        setQuantity(quantity);
    }

    // OOP CONCEPT: Encapsulation - Getters and Setters
    public int getPartId() {
        return partId;
    }

    public void setPartId(int partId) {
        this.partId = partId;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = (partName != null) ? partName.trim() : "";
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        // Validation: price cannot be negative
        if (price < 0.0) {
            throw new IllegalArgumentException("Part price cannot be negative.");
        }
        // OOP CONCEPT: Math API - Round price to 2 decimal places
        this.price = Math.round(price * 100.0) / 100.0;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        // OOP CONCEPT: Math API - Enforce non-negative inventory using Math.max
        this.quantity = Math.max(0, quantity);
    }

    @Override
    public String toString() {
        return partName + " [Rs. " + String.format("%.2f", price) + " | Stock: " + quantity + "]";
    }
}
