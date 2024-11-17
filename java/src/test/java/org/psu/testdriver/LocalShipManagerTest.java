package org.psu.testdriver;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
		assertEquals(1, ships.size());

		// Do it again for lazy loading
		final List<Ship> ships2 = shipManager.getShips();
		assertEquals(1, ships2.size());
	}

}
