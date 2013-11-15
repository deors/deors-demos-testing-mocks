package deors.demos.testing.mocks.staticmocks;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Calendar;

import org.junit.Test;

import deors.demos.testing.mocks.staticmocks.PricingBean;

/**
 * This test does not work unless the database is properly configured and accessible.
 */
public class BrokenPricingBeanTestCase {

    @Test
    public void test() {

        PricingBean pb = new PricingBean();
        Calendar dep = Calendar.getInstance();
        dep.set(Calendar.DAY_OF_MONTH, 15);
        dep.set(Calendar.MONTH, 2);
        dep.clear(Calendar.HOUR_OF_DAY);
        dep.clear(Calendar.MINUTE);
        dep.clear(Calendar.SECOND);
        dep.clear(Calendar.MILLISECOND);

        // imagine multiplier in database is 1.2
        assertEquals(new BigDecimal("150"), pb.getPrice("6180", "Y", dep));
    }
}
