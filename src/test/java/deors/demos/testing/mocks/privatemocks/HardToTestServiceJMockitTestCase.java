package deors.demos.testing.mocks.privatemocks;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class HardToTestServiceJMockitTestCase {

	@Mocked(stubOutClassInitialization = true)
	HardToTestDao dao = null;

	@Test
	public void test1() throws Exception {

		new Expectations() {{
			dao.getMarketShareTotalValue();
			result = 100000;
		}};

		HardToTestService svc = new HardToTestService();
		int value = svc.getMarketShareTotalValue(120);

		assertEquals(12000000, value);

		new Verifications() {{
			dao.getMarketShareTotalValue(); times = 1;
		}};
	}

	@Test
	public void test2() throws Exception {

		new Expectations() {{
			dao.getMarketShareTotalValue();
			result = 100000;
		}};

		HardToTestService svc = new HardToTestService();
		int value = svc.getMarketShareTotalValue(90);

		assertEquals(99910, value);

		new Verifications() {{
			dao.getMarketShareTotalValue(); times = 1;
		}};
	}

	@Test(expected = Exception.class)
	public void test3() throws Exception {

		new Expectations() {{
			dao.getMarketShareTotalValue();
			result = new SQLException();
		}};

		HardToTestService svc = new HardToTestService();
		svc.getMarketShareTotalValue(0);

		new Verifications() {{
			dao.getMarketShareTotalValue(); times = 1;
		}};
	}
}
