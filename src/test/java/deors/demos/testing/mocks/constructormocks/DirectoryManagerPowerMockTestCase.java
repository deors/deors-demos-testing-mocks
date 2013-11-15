package deors.demos.testing.mocks.constructormocks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
//import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.modules.junit4.rule.PowerMockRule;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;

import deors.demos.testing.mocks.constructormocks.DirectoryException;
import deors.demos.testing.mocks.constructormocks.DirectoryManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DirectoryManager.class)
public class DirectoryManagerPowerMockTestCase {

    public DirectoryManagerPowerMockTestCase() {

        super();
    }

    @Test(expected = DirectoryException.class)
    public void testConstructorError() throws Exception {

        LDAPConnection lc = PowerMock.createMock(LDAPConnection.class);
        PowerMock.expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);
        EasyMock.expectLastCall().andThrow(new LDAPException("error", 1, "error"));

        PowerMock.replay(lc, LDAPConnection.class);

        new DirectoryManager("localhost", 2000);
    }

    @Test
    public void testConstructorOk() throws Exception {

        LDAPConnection lc = PowerMock.createMock(LDAPConnection.class);
        PowerMock.expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);

        PowerMock.replay(lc, LDAPConnection.class);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);

        assertNotNull(dm);
        assertTrue(dm.isConnected());

        PowerMock.verify(lc, LDAPConnection.class);
    }

    @Test(expected = DirectoryException.class)
    public void testConstructorErrorAlreadyConnected() throws Exception {

        LDAPConnection lc = PowerMock.createMock(LDAPConnection.class);
        PowerMock.expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);

        PowerMock.replay(lc, LDAPConnection.class);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);

        assertNotNull(dm);
        assertTrue(dm.isConnected());

        dm.createConnection("otherhost", 3000);
    }

    @Test
    public void testCloseConnectionOk() throws Exception {

        LDAPConnection lc = PowerMock.createMock(LDAPConnection.class);
        PowerMock.expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);
        lc.disconnect();

        PowerMock.replay(lc, LDAPConnection.class);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);

        assertNotNull(dm);
        assertTrue(dm.isConnected());

        dm.closeConnection();

        assertFalse(dm.isConnected());

        PowerMock.verify(lc, LDAPConnection.class);
    }

    @Test(expected = DirectoryException.class)
    public void testCloseConnectionError() throws Exception {

        LDAPConnection lc = PowerMock.createMock(LDAPConnection.class);
        PowerMock.expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);
        lc.disconnect();
        EasyMock.expectLastCall().andThrow(new LDAPException("error", 1, "error"));

        PowerMock.replay(lc, LDAPConnection.class);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        dm.closeConnection();
    }
}
