package deors.demos.testing.mocks.constructormocks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;

@RunWith(JMockit.class)
public class DirectoryManagerJMockitTestCase {

	@Mocked(stubOutClassInitialization = true)
	LDAPConnection connection = new LDAPConnection();

    @Test(expected = DirectoryException.class)
    public void testConstructorError() throws Exception {

    	new Expectations() {{
    		connection.connect("localhost", 2000);
    		result = new LDAPException("error", 1, "error");
    	}};

    	new DirectoryManager("localhost", 2000);
	}

    @Test
    public void testConstructorOk() throws Exception {

    	new Expectations() {{
    		connection.connect("localhost", 2000);
    	}};

    	DirectoryManager dm = new DirectoryManager("localhost", 2000);

    	assertNotNull(dm);
        assertTrue(dm.isConnected());

    	new Verifications() {{
    		connection.connect("localhost", 2000); times = 1;
    	}};
    }

    @Test(expected = DirectoryException.class)
    public void testConstructorErrorAlreadyConnected() throws Exception {

    	new Expectations() {{
    		connection.connect("localhost", 2000);
    	}};

    	DirectoryManager dm = new DirectoryManager("localhost", 2000);

    	assertNotNull(dm);
        assertTrue(dm.isConnected());

        new Verifications() {{
    		connection.connect("localhost", 2000); times = 1;
    	}};

        dm.createConnection("otherhost", 3000);
    }

    @Test
    public void testCloseConnectionOk() throws Exception {

    	new Expectations() {{
    		connection.connect("localhost", 2000);
    		connection.disconnect();
    	}};

        DirectoryManager dm = new DirectoryManager("localhost", 2000);

        assertNotNull(dm);
        assertTrue(dm.isConnected());

        dm.closeConnection();

        assertFalse(dm.isConnected());

    	new Verifications() {{
    		connection.connect("localhost", 2000); times = 1;
    		connection.disconnect(); times = 1;
    	}};
    }

    @Test(expected = DirectoryException.class)
    public void testCloseConnectionError() throws Exception {

    	new Expectations() {{
    		connection.connect("localhost", 2000);
    		connection.disconnect();
    		result = new LDAPException("error", 1, "error");
    	}};

    	DirectoryManager dm = new DirectoryManager("localhost", 2000);
    	dm.closeConnection();
    }
}
