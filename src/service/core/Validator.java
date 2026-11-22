package service.core;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Centralized input validation utilities for user-supplied data across the application.
 * 
 * OOP CONCEPT: Encapsulation (Unit 1) - Centralized, reusable business validation rules
 */
public class Validator {

    // Regex for Indian mobile numbers: 10 digits starting with 6, 7, 8, or 9
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    // Regex for Indian motor vehicle registration numbers (e.g. DL01AB1234, PB10A1234, MH12DE4567)
    private static final Pattern REG_PATTERN = Pattern.compile("^[A-Z]{2}\\d{1,2}[A-Z]{1,3}\\d{4}$");

    // Regex for customer names: letters and spaces, 2 to 100 characters
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s.']{2,100}$");

    private Validator() {}

    /**
     * Validates customer name (2-100 alphabetic characters/spaces).
     */
    public static void validateCustomerName(String name) {
        if (name == null || !NAME_PATTERN.matcher(name.trim()).matches()) {
            throw new IllegalArgumentException("Customer name must contain only letters and spaces (2 to 100 characters).");
        }
    }

    /**
     * Validates Indian mobile number (10 digits starting with 6-9).
     */
    public static void validatePhone(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new IllegalArgumentException("Phone number must be exactly 10 digits starting with 6, 7, 8, or 9.");
        }
    }

    /**
     * Validates and normalizes Indian vehicle registration plate string.
     */
    public static String normalizeAndValidateRegNo(String regNo) {
        if (regNo == null) {
            throw new IllegalArgumentException("Registration number cannot be empty.");
        }
        String cleaned = regNo.replaceAll("[\\s-]", "").toUpperCase();
        if (!REG_PATTERN.matcher(cleaned).matches()) {
            throw new IllegalArgumentException("Invalid registration format. Example formats: PB10AB1234, DL01A9999, MH12DE4567.");
        }
        return cleaned;
    }

    /**
     * Validates manufacturer brand and model strings.
     */
    public static void validateBrandAndModel(String brand, String model) {
        if (brand == null || brand.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle brand cannot be empty.");
        }
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle model cannot be empty.");
        }
    }

    /**
     * Validates booking date string (YYYY-MM-DD, cannot be in the past).
     */
    public static void validateBookingDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Booking date is required.");
        }
        try {
            LocalDate date = LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate today = LocalDate.now();
            if (date.isBefore(today)) {
                throw new IllegalArgumentException("Booking date cannot be in the past.");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use YYYY-MM-DD (e.g. " + LocalDate.now() + ").");
        }
    }

    /**
     * Validates spare part price (must be positive).
     */
    public static void validatePartPrice(double price) {
        if (price <= 0.0) {
            throw new IllegalArgumentException("Part price must be greater than Rs. 0.00.");
        }
    }

    /**
     * Validates spare part stock quantity (must be non-negative).
     */
    public static void validatePartQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
    }
}
