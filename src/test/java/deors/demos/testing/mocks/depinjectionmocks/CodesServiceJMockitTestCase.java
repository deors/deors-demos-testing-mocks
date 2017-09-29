package deors.demos.testing.mocks.depinjectionmocks;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class CodesServiceJMockitTestCase {

	@Mocked(stubOutClassInitialization = true)
	CodesDAO mockDAO = null;

    @Test
    public void testSelectAll() {

    	List<Codes> mockData = new ArrayList<Codes>();
        mockData.add(new CodesImpl("A", "active"));
        mockData.add(new CodesImpl("C", "cancelled"));

    	new Expectations() {{
    		mockDAO.selectAll();
    		result = mockData;
    	}};

        CodesService svc = new CodesServiceImpl();
        svc.setCodesDAO(mockDAO);
        Collection<? extends Codes> result = svc.selectAll();

    	new Verifications() {{
    		mockDAO.selectAll(); times = 1;
    	}};

    	assertEquals(mockData, result);
	}
}
