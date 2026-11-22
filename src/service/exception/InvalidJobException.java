package service.exception;

/**
 * Custom checked exception representing business and validation violations
 * within the Vehicle Service Management System (VSMS).
 * 
 * OOP CONCEPT: Exception Handling (Unit 3) - Custom checked exception extending java.lang.Exception
 */
public class InvalidJobException extends Exception {
    
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     * OOP CONCEPT: Constructor Overloading
     */
    public InvalidJobException() {
        super("Invalid job operation or state.");
    }

    /**
     * Parameterized constructor with a specific error message.
     * OOP CONCEPT: Constructor Overloading
     * 
     * @param message Detailed reason for the exception
     */
    public InvalidJobException(String message) {
        super(message);
    }

    /**
     * Parameterized constructor with error message and root cause.
     * OOP CONCEPT: Exception Chaining / Constructor Overloading
     * 
     * @param message Detailed reason for the exception
     * @param cause Root throwable cause
     */
    public InvalidJobException(String message, Throwable cause) {
        super(message, cause);
    }
}
