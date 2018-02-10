package deors.demos.testing.mocks.privatemocks;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import deors.demos.testing.mocks.privatemocks.HardToTestService;

public class BrokenHardToTestServiceTestCase {

    @Ignore("test fails - mocks are needed")
    @Test
    public void test() throws Exception {

        HardToTestService svc = new HardToTestService();

        // assuming dao returns 100,000
        assertEquals(12000000, svc.getMarketShareTotalValue(120));
    }
}
