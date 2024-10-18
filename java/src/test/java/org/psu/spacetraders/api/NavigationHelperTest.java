package org.psu.spacetraders.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.DockResponse;
import org.psu.spacetraders.dto.NavigationRequest;
import org.psu.spacetraders.dto.NavigationResponse;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipNavigation;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testutils.TestRequestThrottler;

/**
 * Tests for {@Link NavigationHelper}
 */
public class NavigationHelperTest {

	/**
	 * Tests the navigate method under normal conditions
	 */
	@Test
	public void navigate() {

		final String waypointSymbol = "waypoint";
		final Waypoint way = mock(Waypoint.class);
		when(way.getSymbol()).thenReturn(waypointSymbol);

		final ShipNavigation shipNav = mock(ShipNavigation.class);
		when(shipNav.getWaypointSymbol()).thenReturn("Some other waypoint");

		final String shipId = "ship";
		final Ship ship = mock(Ship.class);
		when(ship.getSymbol()).thenReturn(shipId);
		when(ship.getNav()).thenReturn(shipNav);

		final Instant arrivalInstant = Instant.ofEpochSecond(100);
		final NavigationResponse navResponse = mock(NavigationResponse.class);
		final ShipNavigation shipNavResponse = mock(ShipNavigation.class, Answers.RETURNS_DEEP_STUBS);
		when(navResponse.getNav()).thenReturn(shipNavResponse);
		when(shipNavResponse.getRoute().getArrival()).thenReturn(arrivalInstant);

		final NavigationClient navClient = mock(NavigationClient.class);
		when(navClient.navigate(eq(shipId), eq(new NavigationRequest(waypointSymbol))))
				.thenReturn(new DataWrapper<NavigationResponse>(navResponse, null));

		final int navPad = 1000;
		final Instant expectedArrival = arrivalInstant.plus(Duration.ofMillis(navPad));

		final NavigationHelper navHelper = new NavigationHelper(navPad, navClient, TestRequestThrottler.get());

		final Instant actualArrival = navHelper.navigate(ship, way);
		assertEquals(expectedArrival, actualArrival);
		verify(ship).setNav(shipNavResponse);
	}

	/**
	 * Tests the navigate method when the ship is already at the waypoint
	 */
	@Test
	public void navigateSameWaypoint() {

		final String waypointSymbol = "waypoint";
		final Waypoint way = mock(Waypoint.class);
		when(way.getSymbol()).thenReturn(waypointSymbol);

		final ShipNavigation shipNav = mock(ShipNavigation.class);
		when(shipNav.getWaypointSymbol()).thenReturn(waypointSymbol);

		final Ship ship = mock(Ship.class);
		when(ship.getNav()).thenReturn(shipNav);

		final int navPad = 1000;

		final NavigationClient navClient = mock(NavigationClient.class);
		final NavigationHelper navHelper = new NavigationHelper(navPad, navClient, TestRequestThrottler.get());

		navHelper.navigate(ship, way);
		verifyNoInteractions(navClient);
		verify(ship, times(0)).setNav(any(ShipNavigation.class));
	}

	/**
	 * Tests dock
	 */
	@Test
	public void dock() {

		final String shipId = "shippy the ship";
		final Ship ship = mock(Ship.class);
		when(ship.getSymbol()).thenReturn(shipId);

		final ShipNavigation shipNav = mock(ShipNavigation.class);
		final DockResponse dockResponse = mock(DockResponse.class);
		when(dockResponse.getNav()).thenReturn(shipNav);
		final NavigationClient navClient = mock(NavigationClient.class);
		when(navClient.dock(shipId)).thenReturn(new DataWrapper<DockResponse>(dockResponse, null));

		final NavigationHelper navHelper = new NavigationHelper(1, navClient, TestRequestThrottler.get());

		navHelper.dock(ship);
		verify(ship).setNav(shipNav);
	}

	/**
	 * Tests orbit
	 */
	@Test
	public void orbit() {

		final String shipId = "shippy the ship";
		final Ship ship = mock(Ship.class);
		when(ship.getSymbol()).thenReturn(shipId);

		final ShipNavigation shipNav = mock(ShipNavigation.class);
		final DockResponse dockResponse = mock(DockResponse.class);
		when(dockResponse.getNav()).thenReturn(shipNav);
		final NavigationClient navClient = mock(NavigationClient.class);
		when(navClient.orbit(shipId)).thenReturn(new DataWrapper<DockResponse>(dockResponse, null));

		final NavigationHelper navHelper = new NavigationHelper(1, navClient, TestRequestThrottler.get());

		navHelper.orbit(ship);
		verify(ship).setNav(shipNav);
	}

}
