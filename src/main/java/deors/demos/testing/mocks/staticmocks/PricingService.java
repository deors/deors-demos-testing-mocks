package deors.demos.testing.mocks.staticmocks;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Example of a service that is highly coupled with JDBC library
 * and as such is not unit testable unless mocking tools are used.
 *
 * @author jorge.hidalgo
 * @version 1.0
 */
public class PricingService {

    /**
     * Calculates and return the price for a flight based on given
     * flight number, ticket class and information retrieved from database.
     *
     * @param flightNumber the flight number
     * @param flightClass the ticket class
     * @return the calculated price
     */
    public BigDecimal getPrice(String flightNumber, String flightClass) {
        BigDecimal basePrice = new BigDecimal("150");
        BigDecimal multiplier = BigDecimal.ONE;

        try {
            Connection conn = DriverManager.getConnection("jdbc:dummy:dummydatabase");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select whatever from table1");
            rs.next();
            multiplier = rs.getBigDecimal("whatever");
        } catch (SQLException e) {
            // exception should be logged
            return null;
        } finally {
            // we should close all objects, don't forget :-)
        }

        if (flightNumber.equals("6180")) {
            basePrice = basePrice.multiply(multiplier);
        } else {
            basePrice = basePrice.divide(multiplier, 2, BigDecimal.ROUND_HALF_UP);
        }

        if (flightNumber.equals("Y")) {
            basePrice = basePrice.divide(multiplier, 2, BigDecimal.ROUND_HALF_UP);
        } else {
            basePrice = basePrice.multiply(multiplier);
        }

        return basePrice;
    }
}
