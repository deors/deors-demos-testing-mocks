package deors.demos.testing.mocks.privatemocks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Example of a data access object that is highly coupled with JDBC library
 * and as such is not unit testable unless mocking tools are used.
 *
 * @author jorge.hidalgo
 * @version 1.0
 */
public class HardToTestDao {

    /**
     * Method that queries the database and returns some value.
     *
     * @return some value from database
     * @throws SQLException an error while accessing the database
     */
    public int getMarketShareTotalValue() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection("jdbc:dummy:dummydatabase");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select whatever from table1");
            rs.next();
            int multiplier = rs.getInt("whatever");
            return multiplier;
        } catch (SQLException e) {
            // exception should be logged
            throw e;
        } finally {
            // we should close all objects, don't forget :-)
        }
    }
}
