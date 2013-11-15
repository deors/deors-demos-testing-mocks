package deors.demos.testing.mocks.privatemocks;

import java.sql.SQLException;

/**
 * Example of a service making use of a data access object that is not
 * unit testable unless mocking tools are used.
 *
 * @author jorge.hidalgo
 * @version 1.0
 */
public class HardToTestService {

    /**
     * Queries some value from the data access object and makes
     * some calculations based on volatility parameter value.
     *
     * @param volatility volatility value
     * @return calculated market share
     * @throws Exception an error while executing the service
     */
    public int getMarketShareTotalValue(int volatility) throws Exception {

        int baseValue = -1;
        try {
            baseValue = getDao().getMarketShareTotalValue();

            if (volatility > 100) {
                baseValue = baseValue * volatility;
            } else {
                baseValue = baseValue - volatility;
            }
        } catch (SQLException e) {
            // do something
            throw new Exception("custom message", e);
        }

        return baseValue;
    }

    /**
     * Creates and initializes the data access object.
     *
     * @return the newly created data access object
     */
    private HardToTestDao getDao() {

        return new HardToTestDao();
    }
}
