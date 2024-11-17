package org.psu.testdriver.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.Ship;
import org.psu.testdriver.LocalShipManager;

/**
 * Tests {@link LocalShipsClient}
 */
@ExtendWith(MockitoExtension.class)
public class LocalShipsClientTest {

	@Mock
	private LocalShipManager localShipManager;

	@InjectMocks
	private LocalShipsClient localShipsClient;

	/**
	 * Tests getShips for the first page
	 */
	@Test
	public void getShipsPage1() {

		final Ship ship1 = mock();
		final Ship ship2 = mock();
		final List<Ship> ships = List.of(ship1, ship2);

		when(localShipManager.getShips()).thenReturn(ships);

		final DataWrapper<List<Ship>> actualShips = localShipsClient.getShips(10, 1);

		assertEquals(ships, actualShips.getData());
		assertEquals(10, actualShips.getMeta().getLimit());
		assertEquals(1, actualShips.getMeta().getPage());
		assertEquals(2, actualShips.getMeta().getTotal());
	}

	/**
	 * Tests getShips for something other than the first page
	 */
	@Test
	public void getShipsPage2() {

		final Ship ship1 = mock();
		final Ship ship2 = mock();
		final List<Ship> ships = List.of(ship1, ship2);

		when(localShipManager.getShips()).thenReturn(ships);

		final DataWrapper<List<Ship>> actualShips = localShipsClient.getShips(10, 2);

		assertEquals(List.of(), actualShips.getData());
		assertEquals(10, actualShips.getMeta().getLimit());
		assertEquals(2, actualShips.getMeta().getPage());
		assertEquals(2, actualShips.getMeta().getTotal());
	}

}
