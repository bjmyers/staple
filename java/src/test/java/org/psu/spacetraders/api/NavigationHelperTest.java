package org.psu.spacetraders.api;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.psu.websocket.WebsocketReporter;

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

		// 100 second flight time
		final Instant depratureInstant = Instant.ofEpochSecond(0);
		final Instant arrivalInstant = Instant.ofEpochSecond(100);
		final NavigationResponse navResponse = mock(NavigationResponse.class);
		final ShipNavigation shipNavResponse = mock(ShipNavigation.class, Answers.RETURNS_DEEP_STUBS);
		when(navResponse.getNav()).thenReturn(shipNavResponse);
		when(shipNavResponse.getRoute().getDepartureTime()).thenReturn(depratureInstant);
		when(shipNavResponse.getRoute().getArrival()).thenReturn(arrivalInstant);

		final NavigationClient navClient = mock(NavigationClient.class);
		when(navClient.navigate(eq(shipId), eq(new NavigationRequest(waypointSymbol))))
				.thenReturn(new DataWrapper<NavigationResponse>(navResponse, null));
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceNavigationClient()).thenReturn(navClient);
		final WebsocketReporter reporter = mock(WebsocketReporter.class);

		final NavigationHelper navHelper = new NavigationHelper(clientProducer, TestRequestThrottler.get(), reporter);

		final Instant actualArrival = navHelper.navigate(ship, way);
		// Arrival will be 100 seconds in the future, to account for test runtime lets just asset that
		// its between 95 and 105 seconds in the future
		assertTrue(Duration.between(Instant.now(), actualArrival).compareTo(Duration.ofSeconds(95)) > 0);
		assertTrue(Duration.between(Instant.now(), actualArrival).compareTo(Duration.ofSeconds(105)) < 0);
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

		final NavigationClient navClient = mock(NavigationClient.class);
		final WebsocketReporter reporter = mock(WebsocketReporter.class);
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceNavigationClient()).thenReturn(navClient);
		final NavigationHelper navHelper = new NavigationHelper(clientProducer, TestRequestThrottler.get(), reporter);

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
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceNavigationClient()).thenReturn(navClient);

		final WebsocketReporter reporter = mock(WebsocketReporter.class);

		final NavigationHelper navHelper = new NavigationHelper(clientProducer, TestRequestThrottler.get(), reporter);

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
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceNavigationClient()).thenReturn(navClient);

		final WebsocketReporter reporter = mock(WebsocketReporter.class);

		final NavigationHelper navHelper = new NavigationHelper(clientProducer, TestRequestThrottler.get(), reporter);

		navHelper.orbit(ship);
		verify(ship).setNav(shipNav);
	}

}
