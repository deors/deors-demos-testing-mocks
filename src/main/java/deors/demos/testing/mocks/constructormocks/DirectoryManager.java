package deors.demos.testing.mocks.constructormocks;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;

/**
 * DirectoryManager component. Manages connections to directories
 * with LDAP and provides some high-level services for querying
 * those directories.
 *
 * @author jorge.hidalgo
 * @version 1.0
 */
public class DirectoryManager {

    /**
     * Flag to signal whether the manager is connected to a directory.
     */
    private boolean connected;

    /**
     * The internal LDAP connection object for the current session.
     */
    private LDAPConnection connection;

    /**
     * Constant flag meaning that LDAP connection is active.
     */
    public static final boolean CONNECTION_ACTIVE = true;

    /**
     * Constant flag meaning that LDAP connection is not active.
     */
    public static final boolean CONNECTION_INACTIVE = false;

    /**
     * Finalizer guardian object. Will try to close an active connection
     * when the object is garbage collected in case it was not
     * explicitly closed before.
     */
    final Object finalizerGuardian = new Object() {

        /**
         * The finalizer method.
         */
        protected void finalize()
            throws java.lang.Throwable {

            try {
                if (isConnected()) {
                    closeConnection();
                }
            } finally {
                super.finalize();
            }
        }
    };

    /**
     * Default constructor.
     */
    public DirectoryManager() {
        super();
    }

    /**
     * Creates the manager with the given directory host and port values,
     * and initializes the connection with the directory.
     *
     * @param directoryHost the directory host
     * @param directoryPort the directory port
     * @throws DirectoryException an exception while creating the connection
     */
    public DirectoryManager(String directoryHost, int directoryPort)
        throws DirectoryException {

        this();
        createConnection(directoryHost, directoryPort);
    }

    /**
     * Closes the current connection with a directory (if active).
     *
     * @throws DirectoryException an exception while closing the connection
     */
    public void closeConnection()
        throws DirectoryException {

        if (!connected) {
            throw new DirectoryException("ERR_CLOSE_CONN_NO");
        }

        if (connection != null) {
            try {
                connection.disconnect();
            } catch (LDAPException ldape) {
                throw new DirectoryException("ERR_CLOSE_CONN", ldape);
            }
        }

        connected = false;
    }

    /**
     * Initializes the connection with the directory.
     *
     * @param directoryHost the directory host
     * @param directoryPort the directory port
     * @throws DirectoryException an exception while creating the connection
     */
    public final void createConnection(String directoryHost, int directoryPort)
        throws DirectoryException {

        if (connected) {
            throw new DirectoryException("ERR_OPEN_CONN_EXISTS");
        }

        if (directoryHost == null || directoryHost.length() == 0 || directoryPort <= 0) {
            throw new IllegalArgumentException("ERR_OPEN_CONN_ARG");
        }

        try {
            connection = new LDAPConnection();
            connection.connect(directoryHost, directoryPort);
        } catch (LDAPException ldape) {
            throw new DirectoryException(
                "ERR_OPEN_CONN", ldape);
        }

        connected = true;
    }

    /**
     * Returns the LDAP connection object for the current session.
     *
     * @return the LDAP connection object
     */
    public LDAPConnection getConnection() {

        return connection;
    }

    /**
     * Returns whether there is an active connection or not.
     *
     * @return the active connection flag
     */
    public boolean isConnected() {
        return connected;
    }
}
