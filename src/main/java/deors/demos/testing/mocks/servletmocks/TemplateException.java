package deors.demos.testing.mocks.servletmocks;

/**
 * Generic exception class for template processing.<br>
 *
 * @author jorge.hidalgo
 * @version 2.5
 */
public final class TemplateException
    extends Exception {

    /**
     * Serialization ID.
     */
    private static final long serialVersionUID = -3713111696958558771L;

    /**
     * Exception constructor.
     */
    public TemplateException() {
        super();
    }

    /**
     * Exception constructor.
     *
     * @param message the exception message
     */
    public TemplateException(String message) {
        super(message);
    }

    /**
     * Exception constructor.
     *
     * @param cause the exception cause
     */
    public TemplateException(Throwable cause) {
        super(cause);
    }

    /**
     * Exception constructor.
     *
     * @param message the exception message
     * @param cause the exception cause
     */
    public TemplateException(String message, Throwable cause) {
        super(message, cause);
    }
}
