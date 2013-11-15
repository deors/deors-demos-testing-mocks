package deors.demos.testing.mocks.depinjectionmocks;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Test;

import deors.demos.testing.mocks.depinjectionmocks.Codes;
import deors.demos.testing.mocks.depinjectionmocks.CodesDAO;
import deors.demos.testing.mocks.depinjectionmocks.CodesImpl;
import deors.demos.testing.mocks.depinjectionmocks.CodesService;
import deors.demos.testing.mocks.depinjectionmocks.CodesServiceImpl;

public class CodesServiceTestCase {

    @Test
    public void testSelectAll() {

        List<Codes> mockData = new ArrayList<Codes>();
        mockData.add(new CodesImpl("A", "active"));
        mockData.add(new CodesImpl("C", "cancelled"));

        CodesDAO mockDAO = EasyMock.createMock(CodesDAO.class);
        mockDAO.selectAll();
        EasyMock.expectLastCall().andReturn(mockData);

        EasyMock.replay(mockDAO);

        CodesService svc = new CodesServiceImpl();
        svc.setCodesDAO(mockDAO);

        Collection<? extends Codes> result = svc.selectAll();

        EasyMock.verify(mockDAO);

        assertEquals(mockData, result);
    }
}
