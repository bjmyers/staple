package org.psu.testdriver;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LocalMarketplaceManager}
 */
public class LocalMarketplaceManagerTest {

	/**
	 * Tests the getMarketInfo method
	 */
	@Test
	public void getMarketInfo() {

		final LocalMarketplaceManager marketplaceManager = new LocalMarketplaceManager();

		assertNotNull(marketplaceManager.getMarketInfo("X1-A1-A0"));
		assertNotNull(marketplaceManager.getMarketInfo("X1-A1-A1"));
		assertNull(marketplaceManager.getMarketInfo("Some other Waypoint"));
	}

}
