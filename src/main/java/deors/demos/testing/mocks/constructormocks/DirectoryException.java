package deors.demos.testing.mocks.constructormocks;

/**
 * Exception class for DirectoryManager component.
 *
 * @author jorge.hidalgo
 * @version 1.0
 */
public class DirectoryException
    extends Exception {

    /**
     * Serialization Id.
     */
    private static final long serialVersionUID = -7154775367885357033L;

    /**
     * Creates a new exception class with the given error message.
     *
     * @param message the error message
     */
    public DirectoryException(String message) {
        super(message);
    }

    /**
     * Creates a new exception class with the given error message
     * and root cause.
     *
     * @param message the error message
     * @param rootCause the root cause
     */
    public DirectoryException(String message, Exception rootCause) {
        super(message, rootCause);
    }
}
