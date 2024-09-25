package org.psu.init;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.api.ShipsClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.DataWrapper.WrapperMetadata;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipNavigation;

/**
 * Tests for {@link ShipLoader}
 */
public class ShipLoaderTest {

	/**
	 * Tests {@link ShipLoader#gatherShips}
	 */
	@Test
	public void gatherShips() {

		// Data will come in two pages
		final int total = 12;
		final int limit = 10;
		final WrapperMetadata metaData = new WrapperMetadata(total, 0, limit);

		final List<Ship> shipsPage1 = List.of(mock(Ship.class));
		final List<Ship> shipsPage2 = List.of(mock(Ship.class));
		final DataWrapper<List<Ship>> shipResponse1 = new DataWrapper<>(shipsPage1, metaData);
		final DataWrapper<List<Ship>> shipResponse2 = new DataWrapper<>(shipsPage2, metaData);

		final ShipsClient shipsClient = mock(ShipsClient.class);
		when(shipsClient.getShips(limit, 1)).thenReturn(shipResponse1);
		when(shipsClient.getShips(limit, 2)).thenReturn(shipResponse2);
		final ShipLoader shipLoader = new ShipLoader(limit, null, shipsClient);

		final List<Ship> ships = shipLoader.gatherShips();

		assertEquals(2, ships.size());
		assertTrue(ships.containsAll(shipsPage1));
		assertTrue(ships.containsAll(shipsPage2));
	}

	/**
	 * Tests {@link ShipLoader#onStartup}
	 */
	@Test
	public void onStartup() {
		final int limit = 20;
		final ShipsClient shipsClient = mock(ShipsClient.class);
		final SystemBuilder systemBuilder = mock(SystemBuilder.class);

		final String systemId = "I'm a system";
		final ShipNavigation shipNav = mock(ShipNavigation.class);
		when(shipNav.getSystemSymbol()).thenReturn(systemId);

		final Ship ship = mock(Ship.class);
		when(ship.getNav()).thenReturn(shipNav);

		final WrapperMetadata metaData = new WrapperMetadata(1, 0, limit);
		final DataWrapper<List<Ship>> shipResponse1 = new DataWrapper<>(List.of(ship), metaData);
		when(shipsClient.getShips(limit, 1)).thenReturn(shipResponse1);

		final ShipLoader shipLoader = new ShipLoader(limit, systemBuilder, shipsClient);

		shipLoader.onStartup(null);

		verify(systemBuilder).gatherWaypoints(systemId);
	}

	/**
	 * Tests {@link ShipLoader#onStartup} when the user has no ships
	 */
	@Test
	public void onStartupNoShips() {
		final int limit = 20;
		final ShipsClient shipsClient = mock(ShipsClient.class);
		final SystemBuilder systemBuilder = mock(SystemBuilder.class);

		final WrapperMetadata metaData = new WrapperMetadata(1, 0, limit);
		// No Ships!
		final DataWrapper<List<Ship>> shipResponse1 = new DataWrapper<>(List.of(), metaData);
		when(shipsClient.getShips(limit, 1)).thenReturn(shipResponse1);

		final ShipLoader shipLoader = new ShipLoader(limit, systemBuilder, shipsClient);

		assertThrows(IllegalStateException.class, () -> shipLoader.onStartup(null));
	}

}