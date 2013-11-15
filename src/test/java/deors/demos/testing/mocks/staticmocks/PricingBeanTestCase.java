package deors.demos.testing.mocks.staticmocks;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Calendar;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import deors.demos.testing.mocks.staticmocks.PricingBean;
import deors.demos.testing.mocks.staticmocks.PricingService;
import deors.demos.testing.mocks.staticmocks.ServiceLocator;

/**
 * This test always works.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ServiceLocator.class) // this is the class we need to intercept
public class PricingBeanTestCase {

    @Test
    public void test1() throws Exception {

        PowerMock.mockStatic(ServiceLocator.class);
        PricingService ps = PowerMock.createMock(PricingService.class);

        EasyMock.expect(ServiceLocator.getPricingService()).andReturn(ps).anyTimes();
        EasyMock.expect(ps.getPrice("6180", "Y")).andReturn(new BigDecimal("150"));

        PowerMock.replay(ServiceLocator.class);
        PowerMock.replay(ps);

        PricingBean pb = new PricingBean();
        Calendar dep = Calendar.getInstance();
        // departure date is 17 days from now
        // test case is that reservation is done more than 15 days before of departure date
        dep.add(Calendar.DAY_OF_MONTH, 17);

        assertTrue(pb.getPrice("6180", "Y", dep).compareTo(new BigDecimal("150")) == 0);
    }

    @Test
    public void test2() throws Exception {

        PowerMock.mockStatic(ServiceLocator.class);
        PricingService ps = PowerMock.createMock(PricingService.class);

        EasyMock.expect(ServiceLocator.getPricingService()).andReturn(ps).anyTimes();
        EasyMock.expect(ps.getPrice("6180", "Y")).andReturn(new BigDecimal("150"));

        PowerMock.replay(ServiceLocator.class);
        PowerMock.replay(ps);

        PricingBean pb = new PricingBean();
        Calendar dep = Calendar.getInstance();
        // departure date is 12 days from now
        // test case is that reservation is done more than 15 days before of departure date
        dep.add(Calendar.DAY_OF_MONTH, 12);

        assertTrue(pb.getPrice("6180", "Y", dep).compareTo(new BigDecimal("180")) == 0);
    }
}
