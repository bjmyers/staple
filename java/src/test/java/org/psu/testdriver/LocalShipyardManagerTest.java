package org.psu.testdriver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.dto.ShipType;

/**
 * Tests for {@link LocalShipyardManager}
 */
public class LocalShipyardManagerTest {

	/**
	 * Tests getShipOfType
	 */
	@Test
	public void getShipOfType() {

		final LocalShipyardManager shipyardManager = new LocalShipyardManager();

		assertNotNull(shipyardManager.getShipOfType(ShipType.SHIP_LIGHT_HAULER));
		assertNotNull(shipyardManager.getShipOfType(ShipType.SHIP_MINING_DRONE));
		assertNull(shipyardManager.getShipOfType(ShipType.SHIP_INTERCEPTOR));
	}

	/**
	 * Tests getShipTypes
	 */
	@Test
	public void getShipTypes() {

		final LocalShipyardManager shipyardManager = new LocalShipyardManager();

		final Collection<ShipType> shipTypes = shipyardManager.getShipTypes();
		assertEquals(2, shipTypes.size());
		assertTrue(shipTypes.contains(ShipType.SHIP_LIGHT_HAULER));
		assertTrue(shipTypes.contains(ShipType.SHIP_MINING_DRONE));

		// Do it again for lazy loading
		final Collection<ShipType> shipTypes2 = shipyardManager.getShipTypes();
		assertEquals(2, shipTypes2.size());
		assertTrue(shipTypes2.contains(ShipType.SHIP_LIGHT_HAULER));
		assertTrue(shipTypes2.contains(ShipType.SHIP_MINING_DRONE));
	}

}
