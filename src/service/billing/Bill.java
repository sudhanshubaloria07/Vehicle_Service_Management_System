package service.billing;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import service.core.ServiceJob;
import service.vehicle.Vehicle;

/**
 * Concrete Bill class that calculates charges, tax and creates an itemized invoice.
 * 
 * OOP CONCEPT: Interface Implementation (Unit 2) - Bill implements Billable
 * OOP CONCEPT: Encapsulation (Unit 1)
 * OOP CONCEPT: Math API (Unit 1) - Math.round() for accurate currency representation
 */
public class Bill implements Billable {

    public static final double GST_RATE = 0.18; // 18% GST

    /**
     * Helper inner class representing an itemized spare part line on the bill.
     */
    public static class LineItem {
        private final String partName;
        private final double unitPrice;
        private final int quantity;
        private final double lineTotal;

        public LineItem(String partName, double unitPrice, int quantity) {
            this.partName = partName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            // OOP CONCEPT: Math API - Rounding currency line item
            this.lineTotal = Math.round((unitPrice * quantity) * 100.0) / 100.0;
        }

        public String getPartName() { return partName; }
        public double getUnitPrice() { return unitPrice; }
        public int getQuantity() { return quantity; }
        public double getLineTotal() { return lineTotal; }
    }

    private ServiceJob job;
    private Vehicle vehicle;
    private List<LineItem> lineItems;
    private String invoiceDate;

    private double labourCharges;
    private double partsTotal;
    private double subtotal;
    private double gstAmount;
    private double grandTotal;

    /**
     * OOP CONCEPT: Constructor Overloading - Default Constructor
     */
    public Bill() {
        this.lineItems = new ArrayList<>();
        this.invoiceDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        this.labourCharges = 0.0;
        this.partsTotal = 0.0;
        this.subtotal = 0.0;
        this.gstAmount = 0.0;
        this.grandTotal = 0.0;
    }

    /**
     * OOP CONCEPT: Constructor Overloading - Parameterized Constructor
     * 
     * @param job     Completed service job
     * @param vehicle Polymorphic Vehicle object
     */
    public Bill(ServiceJob job, Vehicle vehicle) {
        this();
        this.job = job;
        this.vehicle = vehicle;
        this.labourCharges = (job != null) ? job.getLabourCharges() : 0.0;
    }

    public void addLineItem(String partName, double unitPrice, int quantity) {
        lineItems.add(new LineItem(partName, unitPrice, quantity));
    }

    /**
     * Recomputes totals across labour and all line items.
     * OOP CONCEPT: Interface method implementation
     */
    @Override
    public double calculateBill() {
        double sumParts = 0.0;
        for (LineItem item : lineItems) {
            sumParts += item.getLineTotal();
        }
        // OOP CONCEPT: Math API - Rounding to 2 decimal places
        this.partsTotal = Math.round(sumParts * 100.0) / 100.0;
        this.subtotal = Math.round((this.labourCharges + this.partsTotal) * 100.0) / 100.0;
        return this.subtotal;
    }

    /**
     * Calculates 18% GST on the subtotal.
     * OOP CONCEPT: Interface method implementation
     */
    @Override
    public double calculateGST() {
        double rawGst = this.subtotal * GST_RATE;
        // OOP CONCEPT: Math API - Rounding to 2 decimal places
        this.gstAmount = Math.round(rawGst * 100.0) / 100.0;
        return this.gstAmount;
    }

    /**
     * Computes the final payable amount.
     * OOP CONCEPT: Interface method implementation
     */
    @Override
    public double generateFinalAmount() {
        double total = this.subtotal + this.gstAmount;
        // OOP CONCEPT: Math API - Rounding to 2 decimal places
        this.grandTotal = Math.round(total * 100.0) / 100.0;
        return this.grandTotal;
    }

    /**
     * Generates a professionally formatted plain text invoice.
     * 
     * @return Monospaced invoice representation
     */
    public String generateInvoiceText() {
        calculateBill();
        calculateGST();
        generateFinalAmount();

        StringBuilder sb = new StringBuilder();
        String divider = "======================================================================\n";
        String lightDivider = "----------------------------------------------------------------------\n";

        sb.append(divider);
        sb.append("                 AUTOCARE VEHICLE SERVICE CENTRE                      \n");
        sb.append("        Sector 62, Noida, UP - 201309 | Phone: +91 98765 00123         \n");
        sb.append("                       TAX INVOICE / BILL                             \n");
        sb.append(divider);
        sb.append(String.format("Invoice Date: %-25s Job ID: %s\n", invoiceDate, (job != null ? job.getJobId() : "N/A")));
        if (vehicle != null && vehicle.getCustomer() != null) {
            sb.append(String.format("Customer:     %-25s Phone:  %s\n", vehicle.getCustomer().getName(), vehicle.getCustomer().getPhone()));
            sb.append(String.format("Registration: %-25s Type:   %s\n", vehicle.getRegistrationNumber(), vehicle.getVehicleType()));
            // OOP CONCEPT: Runtime Polymorphism - calling getVehicleDetails()
            sb.append(String.format("Vehicle Info: %s\n", vehicle.getVehicleDetails()));
        }
        if (job != null) {
            sb.append(String.format("Service Type: %-25s Status: %s\n", job.getServiceType(), job.getJobStatus()));
        }
        sb.append(lightDivider);
        sb.append(String.format("%-4s %-32s %10s %6s %12s\n", "No.", "Description", "Price (Rs)", "Qty", "Total (Rs)"));
        sb.append(lightDivider);

        // Labour row
        sb.append(String.format("%-4d %-32s %10.2f %6d %12.2f\n", 1, "Labour Charges (" + (job != null ? job.getServiceType() : "Service") + ")", labourCharges, 1, labourCharges));

        int itemIndex = 2;
        for (LineItem item : lineItems) {
            sb.append(String.format("%-4d %-32s %10.2f %6d %12.2f\n",
                    itemIndex++,
                    item.getPartName(),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    item.getLineTotal()));
        }

        sb.append(lightDivider);
        sb.append(String.format("%-44s Rs. %12.2f\n", "Labour Subtotal:", labourCharges));
        sb.append(String.format("%-44s Rs. %12.2f\n", "Parts Subtotal:", partsTotal));
        sb.append(String.format("%-44s Rs. %12.2f\n", "Subtotal (Before Tax):", subtotal));
        sb.append(String.format("%-44s Rs. %12.2f\n", "GST (18%):", gstAmount));
        sb.append(divider);
        sb.append(String.format("%-44s Rs. %12.2f\n", "GRAND TOTAL (Payable):", grandTotal));
        sb.append(divider);
        sb.append("        Thank you for choosing AutoCare! Safe travels ahead.          \n");
        sb.append("       Computer-generated tax invoice. No signature required.         \n");
        sb.append(divider);

        return sb.toString();
    }

    // Getters
    public ServiceJob getJob() { return job; }
    public Vehicle getVehicle() { return vehicle; }
    public List<LineItem> getLineItems() { return lineItems; }
    public String getInvoiceDate() { return invoiceDate; }
    public double getLabourCharges() { return labourCharges; }
    public double getPartsTotal() { return partsTotal; }
    public double getSubtotal() { return subtotal; }
    public double getGstAmount() { return gstAmount; }
    public double getGrandTotal() { return grandTotal; }
}
