package org.psu.spacetraders.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ShipComponent}
 */
public class ShipComponentTest {

	/**
	 * Tests {@link ShipComponent#isMiningLaser}
	 */
	@Test
	public void isMiningLaser() {

		final ShipComponent miningLaser = new ShipComponent("MODULE_MINING_LASER_MK_IV");
		final ShipComponent notMiningLaser = new ShipComponent("MODULE_GAS_SIPHON_MK_IV");

		assertTrue(miningLaser.isMiningLaser());
		assertFalse(notMiningLaser.isMiningLaser());
	}

	/**
	 * Tests {@link ShipComponent#isCargoHold}
	 */
	@Test
	public void isCargoHold() {

		final ShipComponent cargoHold = new ShipComponent("MODULE_CARGO_HOLD_II");
		final ShipComponent notCargoHold = new ShipComponent("MODULE_CREW_QUARTERS_I");

		assertTrue(cargoHold.isCargoHold());
		assertFalse(notCargoHold.isCargoHold());
	}

}
