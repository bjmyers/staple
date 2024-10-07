package org.psu.spacetraders.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.psu.trademanager.dto.TradeRoute;

/**
 * Tests for {@link TradeRoute}
 */
public class TradeRouteTest {

	/**
	 * Tests {@link TradeRoute#isPossible} when the ship is too far from the starting waypoint
	 */
	@Test
	public void isPossibleStartTooFar() {

		final Waypoint importWaypoint = mock(Waypoint.class);
		final Waypoint exportWaypoint = mock(Waypoint.class);
		final Ship ship = mock(Ship.class);
		when(ship.distTo(exportWaypoint)).thenReturn(100.0);

		final FuelStatus fuel = new FuelStatus(50, 50);
		when(ship.getFuel()).thenReturn(fuel);

		final TradeRoute route = new TradeRoute(exportWaypoint, importWaypoint, List.of());

		assertFalse(route.isPossible(ship));
	}

	/**
	 * Tests {@link TradeRoute#isPossible} when the ship is too far from the starting waypoint
	 */
	@Test
	public void isPossibleRouteTooLong() {

		final Waypoint importWaypoint = mock(Waypoint.class);
		final Waypoint exportWaypoint = mock(Waypoint.class);
		final Ship ship = mock(Ship.class);
		when(ship.distTo(exportWaypoint)).thenReturn(100.0);
		when(exportWaypoint.distTo(importWaypoint)).thenReturn(100.0);

		final FuelStatus fuel = new FuelStatus(150, 150);
		when(ship.getFuel()).thenReturn(fuel);

		final TradeRoute route = new TradeRoute(exportWaypoint, importWaypoint, List.of());

		assertFalse(route.isPossible(ship));
	}


	/**
	 * Tests {@link TradeRoute#isPossible} when the route is possible
	 */
	@Test
	public void isPossible() {

		final Waypoint importWaypoint = mock(Waypoint.class);
		final Waypoint exportWaypoint = mock(Waypoint.class);
		final Ship ship = mock(Ship.class);
		when(ship.distTo(exportWaypoint)).thenReturn(100.0);
		when(exportWaypoint.distTo(importWaypoint)).thenReturn(100.0);

		final FuelStatus fuel = new FuelStatus(250, 250);
		when(ship.getFuel()).thenReturn(fuel);

		final TradeRoute route = new TradeRoute(exportWaypoint, importWaypoint, List.of());

		assertTrue(route.isPossible(ship));
	}

	/**
	 * Tests {@link TradeRoute#getDistance}
	 */
	@Test
	public void getDistance() {

		final Waypoint importWaypoint = mock(Waypoint.class);
		when(importWaypoint.getX()).thenReturn(0);
		when(importWaypoint.getY()).thenReturn(0);
		final Waypoint exportWaypoint = mock(Waypoint.class);
		when(exportWaypoint.getX()).thenReturn(3);
		when(exportWaypoint.getY()).thenReturn(4);

		final TradeRoute route = new TradeRoute(exportWaypoint, importWaypoint, List.of());

		assertEquals(5.0, route.getDistance(), 1e-9);
	}

}
