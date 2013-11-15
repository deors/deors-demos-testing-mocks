package deors.demos.testing.mocks.staticmocks;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Example of a bean that calculates prices based on data provided by
 * a statically initialized pricing service. It is not unit testable
 * unless a mock framework is used.
 *
 * @author jorge.hidalgo
 * @version 1.0
 */
public class PricingBean {

    /**
     * Calculates and returns the price for a flight.
     *
     * @param flightNumber the flight number
     * @param flightClass the ticket class
     * @param departureDate the departure date
     * @return the calculated price
     */
    public BigDecimal getPrice(String flightNumber, String flightClass, Calendar departureDate) {

        PricingService ps = ServiceLocator.getPricingService();

        BigDecimal price = ps.getPrice(flightNumber, flightClass);

        Calendar now = Calendar.getInstance();
        Calendar cutDate = (Calendar) departureDate.clone();
        cutDate.add(Calendar.DAY_OF_MONTH, -15);
        if (now.after(cutDate)) {
            price = price.multiply(new BigDecimal("1.2"));
        }

        return price;
    }
}
