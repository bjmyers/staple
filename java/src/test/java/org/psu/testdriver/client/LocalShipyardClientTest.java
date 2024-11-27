package org.psu.testdriver.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link LocalShipyardClient}
 */
public class LocalShipyardClientTest {

	/**
	 * Tests getShipyardData
	 */
	@Test
	public void getShipyardData() {

		final LocalShipyardClient shipyardClient = new LocalShipyardClient();

		assertNotNull(shipyardClient.getShipyardData("system", "system-waypoint"));
	}

}
