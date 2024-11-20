package org.psu.testdriver.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.DockResponse;
import org.psu.spacetraders.dto.FuelStatus;
import org.psu.spacetraders.dto.NavigationRequest;
import org.psu.spacetraders.dto.NavigationResponse;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipNavigation;
import org.psu.spacetraders.dto.ShipNavigation.Status;
import org.psu.spacetraders.dto.ShipRoute;
import org.psu.spacetraders.dto.ShipRoute.RoutePoint;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testdriver.LocalShipManager;
import org.psu.testdriver.LocalWaypointsManager;

/**
 * Tests for {@link LocalNavigationClient}
 */
@ExtendWith(MockitoExtension.class)
public class LocalNavigationClientTest {

	@Mock
	private LocalShipManager shipManager;

	@Mock
	private LocalWaypointsManager waypointsManager;

	@InjectMocks
	private LocalNavigationClient navigationClient;

	/**
	 * Tests the orbit method
	 */
	@Test
	public void orbit() {
		final String shipId = "shippy";
		final Ship ship = new Ship();
		final ShipNavigation shipNav = new ShipNavigation();
		ship.setNav(shipNav);
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final DataWrapper<DockResponse> response = navigationClient.orbit(shipId);

		assertNull(response.getMeta());
		assertEquals(Status.IN_ORBIT, response.getData().getNav().getStatus());
		assertEquals(Status.IN_ORBIT, ship.getNav().getStatus());
	}

	/**
	 * Tests the dock method
	 */
	@Test
	public void dock() {
		final String shipId = "shippy";
		final Ship ship = new Ship();
		final ShipNavigation shipNav = new ShipNavigation();
		ship.setNav(shipNav);
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final DataWrapper<DockResponse> response = navigationClient.dock(shipId);

		assertNull(response.getMeta());
		assertEquals(Status.DOCKED, response.getData().getNav().getStatus());
		assertEquals(Status.DOCKED, ship.getNav().getStatus());
	}

	/**
	 * Tests the navigate method when the ship is not yet at its destination
	 */
	@Test
	public void navigateNotReady() {
		final String shipId = "shippy";
		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		final ShipRoute shipRoute = mock();
		when(shipNav.getRoute()).thenReturn(shipRoute);
		when(ship.getNav()).thenReturn(shipNav);

		// Will arrive in the future
		when(shipRoute.getArrival()).thenReturn(Instant.now().plus(Duration.ofDays(1)));

		when(shipManager.getShip(shipId)).thenReturn(ship);

		final String destinationId = "waypoint";
		final Waypoint destination = mock();
		when(waypointsManager.getWaypoint(destinationId)).thenReturn(destination);
		final NavigationRequest navRequest = new NavigationRequest(destinationId);

		assertThrows(IllegalStateException.class, () -> navigationClient.navigate(shipId, navRequest));
	}

	/**
	 * Tests the navigate method when the destination is too far away
	 */
	@Test
	public void navigateTooFar() {
		final String shipId = "shippy";
		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		final ShipRoute shipRoute = mock();
		when(shipNav.getRoute()).thenReturn(shipRoute);
		when(ship.getNav()).thenReturn(shipNav);

		// Ship has already arrived at its current location
		when(shipRoute.getArrival()).thenReturn(Instant.now().minus(Duration.ofDays(1)));

		when(shipManager.getShip(shipId)).thenReturn(ship);

		final String destinationId = "waypoint";
		final Waypoint destination = mock();
		when(waypointsManager.getWaypoint(destinationId)).thenReturn(destination);
		final NavigationRequest navRequest = new NavigationRequest(destinationId);

		final String departurePointId = "departurePoint";
		final Waypoint departurePoint = mock();
		when(waypointsManager.getWaypoint(departurePointId)).thenReturn(departurePoint);
		when(shipNav.getWaypointSymbol()).thenReturn(departurePointId);
		when(departurePoint.distTo(destination)).thenReturn(100.0);

		// Not enough fuel to cover the distance
		final FuelStatus currentFuel = new FuelStatus(50, 200);
		when(ship.getFuel()).thenReturn(currentFuel);

		assertThrows(IllegalStateException.class, () -> navigationClient.navigate(shipId, navRequest));
	}

	/**
	 * Tests the navigate method
	 */
	@Test
	public void navigate() {
		final String shipId = "shippy";
		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		final ShipRoute shipRoute = mock();
		when(shipNav.getRoute()).thenReturn(shipRoute);
		when(ship.getNav()).thenReturn(shipNav);

		final RoutePoint destinationPoint = mock();
		when(shipRoute.getDestination()).thenReturn(destinationPoint);

		// Ship has already arrived at its current location
		when(shipRoute.getArrival()).thenReturn(Instant.now().minus(Duration.ofDays(1)));

		when(shipManager.getShip(shipId)).thenReturn(ship);

		final String destinationId = "waypoint";
		final Waypoint destination = mock();
		when(destination.getSymbol()).thenReturn(destinationId);
		when(destination.getX()).thenReturn(0);
		when(destination.getY()).thenReturn(1);
		when(waypointsManager.getWaypoint(destinationId)).thenReturn(destination);
		final NavigationRequest navRequest = new NavigationRequest(destinationId);

		final String departurePointId = "departurePoint";
		final Waypoint departurePoint = mock();
		when(waypointsManager.getWaypoint(departurePointId)).thenReturn(departurePoint);
		when(shipNav.getWaypointSymbol()).thenReturn(departurePointId);
		when(departurePoint.distTo(destination)).thenReturn(99.5);

		// Enough fuel to cover the distance
		final FuelStatus currentFuel = new FuelStatus(150, 200);
		when(ship.getFuel()).thenReturn(currentFuel);

		final DataWrapper<NavigationResponse> response = navigationClient.navigate(shipId, navRequest);

		final FuelStatus expectedNewFuel = new FuelStatus(50, 200);
		verify(ship).setFuel(expectedNewFuel);
		verify(shipRoute).setOrigin(destinationPoint);
		verify(shipNav).setWaypointSymbol(destinationId);

		assertNull(response.getMeta());
		assertEquals(shipNav, response.getData().getNav());
		assertEquals(expectedNewFuel, response.getData().getFuelStatus());

		final ArgumentCaptor<RoutePoint> newDestinationCaptor = ArgumentCaptor.forClass(RoutePoint.class);
		verify(shipRoute).setDestination(newDestinationCaptor.capture());
		final RoutePoint newDestination = newDestinationCaptor.getValue();
		assertEquals(destinationId, newDestination.getSymbol());
		assertEquals(0, newDestination.getX());
		assertEquals(1, newDestination.getY());
	}

}
