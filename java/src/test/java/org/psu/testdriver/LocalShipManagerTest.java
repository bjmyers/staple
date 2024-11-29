package org.psu.testdriver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.dto.Ship;

/**
 * Tests for {@link LocalShipManager}
 */
public class LocalShipManagerTest {

	/**
	 * Tests getShips
	 */
	@Test
	public void getShips() {
		final LocalShipManager shipManager = new LocalShipManager();

		final List<Ship> ships = shipManager.getShips();
		assertEquals(2, ships.size());

		// Do it again for lazy loading
		final List<Ship> ships2 = shipManager.getShips();
		assertEquals(2, ships2.size());
	}

	/**
	 * Tests getShip
	 */
	@Test
	public void getShip() {
		final LocalShipManager shipManager = new LocalShipManager();

		final Ship ship = shipManager.getShip("Shippy-1");
		assertNotNull(ship);

		final Ship ship2 = shipManager.getShip("Some other ship");
		assertNull(ship2);
	}

	/**
	 * Tests addShip
	 */
	@Test
	public void addShip() {

		final LocalShipManager shipManager = new LocalShipManager();

		final String shipId = "new ship";
		final Ship ship = mock();
		when(ship.getSymbol()).thenReturn(shipId);

		shipManager.addShip(ship);

		final List<Ship> ships = shipManager.getShips();
		assertEquals(3, ships.size());
		assertTrue(ships.contains(ship));
	}

}
