package deors.demos.testing.mocks.constructormocks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import deors.demos.testing.mocks.constructormocks.DirectoryException;
import deors.demos.testing.mocks.constructormocks.DirectoryManager;

public class DirectoryManagerTestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public DirectoryManagerTestCase() {

        super();
    }

    @Test
    public void testDefaultConstructor() {

        DirectoryManager dm = new DirectoryManager();
        assertNotNull(dm);
        assertFalse(dm.isConnected());
        assertNull(dm.getConnection());
    }

    @Test
    public void testConstructorIAE1() throws DirectoryException {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("ERR_OPEN_CONN_ARG");

        new DirectoryManager(null, 2000);
    }

    @Test
    public void testConstructorIAE2() throws DirectoryException {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("ERR_OPEN_CONN_ARG");

        new DirectoryManager("", 2000);
    }

    @Test
    public void testConstructorIAE3() throws DirectoryException {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("ERR_OPEN_CONN_ARG");

        new DirectoryManager("localhost", -1);
    }

    @Test
    public void testCloseNotConnected() throws DirectoryException {

        thrown.expect(DirectoryException.class);
        thrown.expectMessage("ERR_CLOSE_CONN_NO");

        DirectoryManager dm = new DirectoryManager();
        dm.closeConnection();
    }
}
