package deors.demos.testing.mocks.staticmocks;

/**
 * Example of a service locator that statically initializes the pricing
 * service. It is not unit testable unless a mock framework is used.
 *
 * @author jorge.hidalgo
 * @version 1.0
 */
public class ServiceLocator {

    /**
     * The pricing service.
     */
    private static PricingService pricingService;

    /**
     * Lazily creates and initialized the pricing service.
     *
     * @return the pricing service
     */
    public static PricingService getPricingService() {
        if (pricingService == null) {
            pricingService = new PricingService();
        }
        return pricingService;
    }
}
