package deors.demos.testing.mocks.staticmocks;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class PricingBeanJMockitTestCase {

	@Mocked(stubOutClassInitialization = true)
	ServiceLocator serviceLocator = null;
	
	@Mocked(stubOutClassInitialization = true)
	PricingService pricingService = null;
	
    @Test
    public void test1() throws Exception {

    	final int year = 2014;
        final int month = Calendar.JANUARY;
    	Calendar now = new GregorianCalendar(year, month, 10);
    	
    	new Expectations() {{
    		ServiceLocator.getPricingService();
    		result = pricingService;
    		
    		pricingService.getPrice("6180", "Y");
    		result = new BigDecimal("150");
    	}};
    	
    	new Expectations(Calendar.class) {{
    		Calendar.getInstance();
    		result = now;    		
    	}};

        PricingBean pb = new PricingBean();
        Calendar dep = new GregorianCalendar(year, month, 30);
        
        BigDecimal out = pb.getPrice("6180", "Y", dep);
        
        assertTrue(new BigDecimal("150").compareTo(out) == 0);
        
        new Verifications() {{
        	ServiceLocator.getPricingService(); times = 1;
        	pricingService.getPrice("6180", "Y"); times = 1;
        }};
	}
    
    @Test
    public void test2() throws Exception {
    	
    	final int year = 2014;
        final int month = Calendar.JANUARY;
    	Calendar now = new GregorianCalendar(year, month, 18);
    	
    	new Expectations() {{
    		ServiceLocator.getPricingService();
    		result = pricingService;
    		
    		pricingService.getPrice("6180", "Y");
    		result = new BigDecimal("150");
            
    	}};
    	
    	new Expectations(Calendar.class) {{
    		Calendar.getInstance();
    		result = now;    		
    	}};
    	
        PricingBean pb = new PricingBean();
        Calendar dep = new GregorianCalendar(year, month, 30);

        BigDecimal out = pb.getPrice("6180", "Y", dep);
        System.out.println(out);
        
        assertTrue(new BigDecimal("180").compareTo(out) == 0);
        
        new Verifications() {{
        	ServiceLocator.getPricingService(); times = 1;
        	pricingService.getPrice("6180", "Y"); times = 1;
        }};    	
    }
}
