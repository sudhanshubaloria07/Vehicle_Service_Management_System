package service.billing;

/**
 * Interface defining the billing contract for serviced vehicles.
 * 
 * OOP CONCEPT: Interface (Unit 2) - Pure contract defining standard calculation methods
 */
public interface Billable {

    /**
     * Calculates the subtotal amount before taxes (Labour + Parts).
     * 
     * @return Subtotal in INR
     */
    double calculateBill();

    /**
     * Calculates Goods and Services Tax (GST at 18%).
     * 
     * @return GST amount in INR
     */
    double calculateGST();

    /**
     * Generates the grand final payable total (Subtotal + GST).
     * 
     * @return Final amount in INR
     */
    double generateFinalAmount();
}
