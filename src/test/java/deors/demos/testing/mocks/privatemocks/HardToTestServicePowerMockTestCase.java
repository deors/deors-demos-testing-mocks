package deors.demos.testing.mocks.privatemocks;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import deors.demos.testing.mocks.privatemocks.HardToTestDao;
import deors.demos.testing.mocks.privatemocks.HardToTestService;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HardToTestService.class) // this is the class with a private field
public class HardToTestServicePowerMockTestCase {

    @Test
    public void test1() throws Exception {

        HardToTestService svc = PowerMock.createPartialMock(HardToTestService.class, "getDao");
        HardToTestDao dao = PowerMock.createMock(HardToTestDao.class);

        PowerMock.expectPrivate(svc, "getDao").andReturn(dao);
        EasyMock.expect(dao.getMarketShareTotalValue()).andReturn(100000);

        PowerMock.replay(svc);
        PowerMock.replay(dao);

        assertEquals(12000000, svc.getMarketShareTotalValue(120));
    }

    @Test
    public void test2() throws Exception {

        HardToTestService svc = PowerMock.createPartialMock(HardToTestService.class, "getDao");
        HardToTestDao dao = PowerMock.createMock(HardToTestDao.class);

        PowerMock.expectPrivate(svc, "getDao").andReturn(dao);
        EasyMock.expect(dao.getMarketShareTotalValue()).andReturn(100000);

        PowerMock.replay(svc);
        PowerMock.replay(dao);

        assertEquals(99910, svc.getMarketShareTotalValue(90));
    }

    @Test(expected = Exception.class)
    public void test3() throws Exception {

        HardToTestService svc = PowerMock.createPartialMock(HardToTestService.class, "getDao");
        HardToTestDao dao = PowerMock.createMock(HardToTestDao.class);

        PowerMock.expectPrivate(svc, "getDao").andReturn(dao);
        EasyMock.expect(dao.getMarketShareTotalValue()).andThrow(new SQLException());

        PowerMock.replay(svc);
        PowerMock.replay(dao);

        svc.getMarketShareTotalValue(0);
    }
}
