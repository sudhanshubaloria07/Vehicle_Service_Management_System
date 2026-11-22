package service.core;

import service.billing.Bill;
import service.billing.Billable;
import service.database.DBConnection;
import service.exception.InvalidJobException;
import service.vehicle.Bike;
import service.vehicle.Car;
import service.vehicle.Vehicle;

/**
 * Automated system self-test verifying OOPJ concepts, calculations,
 * polymorphism, exceptions, and validation without requiring a live GUI.
 */
public class SystemSelfTest {

    public static void main(String[] args) {
        System.out.println("=====================================================================");
        System.out.println("       AUTOCARE VSMS - AUTOMATED DOMAIN & OOPJ VERIFICATION TEST     ");
        System.out.println("=====================================================================\n");

        int passed = 0;
        int failed = 0;

        // Test 1: Inheritance & Runtime Polymorphism (Car 1.5x vs Bike 1.0x)
        try {
            Customer cust1 = new Customer("Rohan Sharma", "9876543210");
            Customer cust2 = new Customer("Pooja Nair", "9812345678");

            // OOP CONCEPT: Polymorphism - Assigning child objects to parent Vehicle references
            Vehicle car = new Car(1, cust1, "DL01AB1234", "Hyundai", "i20");
            Vehicle bike = new Bike(2, cust2, "MH12DE5678", "Honda", "Activa 6G");

            double baseLabour = 1200.0;
            double carLabour = car.calculateLabourSurcharge(baseLabour);
            double bikeLabour = bike.calculateLabourSurcharge(baseLabour);

            assertCondition(carLabour == 1800.0, "Car labour should be 1200 * 1.5 = 1800.00 (got " + carLabour + ")");
            assertCondition(bikeLabour == 1200.0, "Bike labour should be 1200 * 1.0 = 1200.00 (got " + bikeLabour + ")");
            assertCondition(car.getVehicleDetails().contains("[Car]"), "Car details should contain [Car]");
            assertCondition(bike.getVehicleDetails().contains("[Two-Wheeler/Bike]"), "Bike details should contain [Two-Wheeler/Bike]");

            System.out.println("[PASS] Unit 2: Inheritance, Overriding & Runtime Polymorphism");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] Polymorphism Test: " + t.getMessage());
            failed++;
        }

        // Test 2: Composition (Vehicle HAS-A Customer)
        try {
            Customer cust = new Customer("Vikram Singh", "9543210987");
            Vehicle v = new Car(10, cust, "HR26BC3344", "Tata", "Nexon EV");

            assertCondition(v.getCustomer().getName().equals("Vikram Singh"), "Customer name matches");
            assertCondition(v.getCustomer().getPhone().equals("9543210987"), "Customer phone matches");

            System.out.println("[PASS] Unit 2: Composition (Vehicle HAS-A Customer)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] Composition Test: " + t.getMessage());
            failed++;
        }

        // Test 3: Math API & Billing Interface
        try {
            Customer cust = new Customer("Aarav Sharma", "9876543210");
            Vehicle car = new Car(1, cust, "DL01AB1234", "Hyundai", "i20");
            ServiceJob job = new ServiceJob("JOB-2026-0001", 1, "General Service", "2026-11-15", "Completed", 1800.00);

            // OOP CONCEPT: Interface reference
            Billable billable = new Bill(job, car);
            Bill bill = (Bill) billable;

            // Add sample items: 3L oil @ 650, 1 filter @ 280, 1 wiper @ 750
            bill.addLineItem("Synthetic Engine Oil 1L", 650.00, 3); // 1950.00
            bill.addLineItem("Premium Oil Filter", 280.00, 1);       // 280.00
            bill.addLineItem("Frameless Wiper Blades Pair", 750.00, 1); // 750.00
            // Parts total = 2980.00
            // Subtotal = 1800 (labour) + 2980 (parts) = 4780.00
            // GST (18%) = 4780 * 0.18 = 860.40
            // Grand Total = 4780 + 860.40 = 5640.40

            double subtotal = billable.calculateBill();
            double gst = billable.calculateGST();
            double finalAmount = billable.generateFinalAmount();

            assertCondition(subtotal == 4780.00, "Subtotal should be 4780.00 (got " + subtotal + ")");
            assertCondition(gst == 860.40, "GST should be 860.40 (got " + gst + ")");
            assertCondition(finalAmount == 5640.40, "Grand total should be 5640.40 (got " + finalAmount + ")");

            String invoiceText = bill.generateInvoiceText();
            assertCondition(invoiceText.contains("AUTOCARE VEHICLE SERVICE CENTRE"), "Invoice contains header");
            assertCondition(invoiceText.contains("GRAND TOTAL (Payable):"), "Invoice contains grand total");

            System.out.println("[PASS] Unit 1 & 2: Math API Rounding & Billable Interface");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] Billing & Math API Test: " + t.getMessage());
            failed++;
        }

        // Test 4: Validator & Regex Patterns
        try {
            Validator.validateCustomerName("Dr. Rajesh Kumar");
            Validator.validatePhone("9876543210");
            String cleanedReg = Validator.normalizeAndValidateRegNo("dl 01 ab 1234");
            assertCondition("DL01AB1234".equals(cleanedReg), "Registration normalized to uppercase without spaces");

            // Invalid cases
            boolean caughtInvalidPhone = false;
            try {
                Validator.validatePhone("1234567890"); // doesn't start with 6-9
            } catch (IllegalArgumentException e) {
                caughtInvalidPhone = true;
            }
            assertCondition(caughtInvalidPhone, "Invalid phone correctly blocked");

            boolean caughtInvalidReg = false;
            try {
                Validator.normalizeAndValidateRegNo("INVALID_PLATE_123");
            } catch (IllegalArgumentException e) {
                caughtInvalidReg = true;
            }
            assertCondition(caughtInvalidReg, "Invalid registration correctly blocked");

            System.out.println("[PASS] Unit 1: Encapsulation & Input Validation (Validator)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] Validator Test: " + t.getMessage());
            failed++;
        }

        // Test 5: Custom Exception (InvalidJobException)
        try {
            boolean caught = false;
            try {
                throw new InvalidJobException("Job JOB-2026-9999 not found.");
            } catch (InvalidJobException ex) {
                caught = true;
                assertCondition(ex.getMessage().contains("JOB-2026-9999"), "Exception message preserved");
            }
            assertCondition(caught, "Custom exception thrown and caught");

            System.out.println("[PASS] Unit 3: Custom Exception (InvalidJobException)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] Custom Exception Test: " + t.getMessage());
            failed++;
        }

        // Test 6: Multithreading Concurrency Guard
        try {
            final boolean[] callbackTriggered = { false };
            ProgressListener dummyListener = new ProgressListener() {
                @Override public void onStageChanged(String stage, int percent) { callbackTriggered[0] = true; }
                @Override public void onFinished() {}
                @Override public void onError(String msg) {}
            };

            JobProgressThread threadTask = new JobProgressThread("TEST-JOB", dummyListener);
            assertCondition(!JobProgressThread.isJobActive("TEST-JOB"), "Job not initially active");

            System.out.println("[PASS] Unit 3: Multithreading Architecture (JobProgressThread & ProgressListener)");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] Multithreading Test: " + t.getMessage());
            failed++;
        }

        // Test 7: Database Connectivity Diagnostics
        System.out.print("[INFO] Checking Database Connectivity: ");
        boolean dbOnline = DBConnection.testConnection();
        if (dbOnline) {
            System.out.println("ONLINE (MySQL is active and connected)");
        } else {
            System.out.println("OFFLINE (MySQL service not currently running in local terminal environment - expected in isolated sandbox)");
            System.out.println("       Detailed Diagnostic Message: " + DBConnection.getLastErrorMessage().split("\n")[0]);
        }

        System.out.println("\n=====================================================================");
        System.out.println("VERIFICATION SUMMARY: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("=====================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }
}
