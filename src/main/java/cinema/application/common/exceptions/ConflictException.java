package cinema.application.common.exceptions;

/**
 * Exception thrown when a conflict is detected.
 */
public class ConflictException extends RuntimeException { 
    public ConflictException(String message) { 
        super(message); 
    } 
}
