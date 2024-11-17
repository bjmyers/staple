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
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testdriver.LocalWaypointsManager;

/**
 * Test for {@link LocalWaypointsClient}
 */
@ExtendWith(MockitoExtension.class)
public class LocalWaypointsClientTest {

	@Mock
	private LocalWaypointsManager localWaypointsManager;

	@InjectMocks
	private LocalWaypointsClient localWaypointsClient;

	/**
	 * Tests getWaypoints for the first page
	 */
	@Test
	public void getWaypointsPage1() {

		final Waypoint way1 = mock();
		final Waypoint way2 = mock();
		final List<Waypoint> ways = List.of(way1, way2);

		when(localWaypointsManager.getWaypoints()).thenReturn(ways);

		final DataWrapper<List<Waypoint>> actualWaypoints = localWaypointsClient.getWaypoints("system", 10, 1);

		assertEquals(ways, actualWaypoints.getData());
		assertEquals(10, actualWaypoints.getMeta().getLimit());
		assertEquals(1, actualWaypoints.getMeta().getPage());
		assertEquals(2, actualWaypoints.getMeta().getTotal());
	}

	/**
	 * Tests getShips for something other than the first page
	 */
	@Test
	public void getShipsPage2() {

		final Waypoint way1 = mock();
		final Waypoint way2 = mock();
		final List<Waypoint> ways = List.of(way1, way2);

		when(localWaypointsManager.getWaypoints()).thenReturn(ways);

		final DataWrapper<List<Waypoint>> actualWaypoints = localWaypointsClient.getWaypoints("system", 10, 2);

		assertEquals(List.of(), actualWaypoints.getData());
		assertEquals(10, actualWaypoints.getMeta().getLimit());
		assertEquals(2, actualWaypoints.getMeta().getPage());
		assertEquals(2, actualWaypoints.getMeta().getTotal());
	}

}
