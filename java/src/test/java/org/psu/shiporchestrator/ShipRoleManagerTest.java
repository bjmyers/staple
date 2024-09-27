package org.psu.shiporchestrator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipComponent;

/**
 * Tests for {@link ShipRoleManager}
 */
public class ShipRoleManagerTest {

	private final ShipRoleManager roleManager = new ShipRoleManager();

	/**
	 * Tests {@link ShipRoleManager#determineRole} for a mining ship
	 */
	@Test
	public void determineRoleMiningShip() {
		final ShipComponent component1 = new ShipComponent("MOUNT_X");
		final ShipComponent component2 = new ShipComponent("MOUNT_MINING_LASER");
		final ShipComponent component3 = new ShipComponent("MODULE_Z");

		final Ship ship = mock(Ship.class);
		when(ship.getMounts()).thenReturn(List.of(component1, component2));
		when(ship.getModules()).thenReturn(List.of(component3));

		assertEquals(ShipRole.MINING, roleManager.determineRole(ship));
	}

	/**
	 * Tests {@link ShipRoleManager#determineRole} for a trading ship
	 */
	@Test
	public void determineRoleTradingShip() {
		final ShipComponent component1 = new ShipComponent("MOUNT_X");
		final ShipComponent component2 = new ShipComponent("MOUNT_Y");
		final ShipComponent component3 = new ShipComponent("MODULE_CARGO_HOLD_II");

		final Ship ship = mock(Ship.class);
		when(ship.getMounts()).thenReturn(List.of(component1, component2));
		when(ship.getModules()).thenReturn(List.of(component3));

		assertEquals(ShipRole.TRADE, roleManager.determineRole(ship));
	}


	/**
	 * Tests {@link ShipRoleManager#determineRole} for a probe ship
	 */
	@Test
	public void determineRoleProbeShip() {
		final ShipComponent component1 = new ShipComponent("MOUNT_X");
		final ShipComponent component2 = new ShipComponent("MODULE_Y");

		final Ship ship = mock(Ship.class);
		when(ship.getMounts()).thenReturn(List.of(component1));
		when(ship.getModules()).thenReturn(List.of(component2));

		assertEquals(ShipRole.PROBE, roleManager.determineRole(ship));
	}

}
