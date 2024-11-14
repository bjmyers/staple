package org.psu.navigation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.psu.spacetraders.dto.FuelStatus;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipRoute.RoutePoint;
import org.psu.spacetraders.dto.Waypoint;

/**
 * Tests for {@link RefuelPathCalculator}
 */
public class RefuelPathCalculatorTest {

	/**
	 * Tests the case where the origin and the destination are both in the original map
	 */
	@Test
	public void originAndDestinationInMap() {

		final Waypoint origin = makeWaypoint(0, 0);

		// The intermediate waypoints exist on the line x = 100
		final Waypoint way1 = makeWaypoint(100, -20);
		final Waypoint way2 = makeWaypoint(100, -10);
		final Waypoint way3 = makeWaypoint(100, 0);
		final Waypoint way4 = makeWaypoint(100, 10);
		final Waypoint way5 = makeWaypoint(100, 20);

		final Waypoint destination = makeWaypoint(200, 0);

		// Quickest route is through way3
		final RefuelPathCalculator calculator = new RefuelPathCalculator();
		calculator.loadRefuelWaypoints(List.of(origin, way1, way2, way3, way4, way5, destination));

		// Enough fuel to go from origin to any of the intermediate waypoints, but not
		// right to the destination
		final NavigationPath path = calculator.determineShortestRoute(origin, destination, 150, 150);

		assertEquals(200, path.getLength(), 1e-9);
		assertEquals(List.of(way3, destination), path.getWaypoints());
	}

	/**
	 * Tests the case where the origin and the destination are not in the original map
	 */
	@Test
	public void originAndDestinationNotInMap() {

		final Waypoint origin = makeWaypoint(0, 0);

		// The intermediate waypoints exist on the line x = 100
		final Waypoint way1 = makeWaypoint(100, -20);
		final Waypoint way2 = makeWaypoint(100, -10);
		final Waypoint way3 = makeWaypoint(100, 0);
		final Waypoint way4 = makeWaypoint(100, 10);
		final Waypoint way5 = makeWaypoint(100, 20);

		final Waypoint destination = makeWaypoint(200, 0);

		// Quickest route is through way3
		final RefuelPathCalculator calculator = new RefuelPathCalculator();
		calculator.loadRefuelWaypoints(List.of(way1, way2, way3, way4, way5));

		// Enough fuel to go from origin to any of the intermediate waypoints, but not
		// right to the destination
		final NavigationPath path = calculator.determineShortestRoute(origin, destination, 150, 150);

		assertEquals(200, path.getLength(), 1e-9);
		assertEquals(List.of(way3, destination), path.getWaypoints());
	}

	/**
	 * Tests the case where the origin and the destination are not in the original
	 * map using the method which takes a ship
	 */
	@Test
	public void originAndDestinationNotInMapShip() {

		// The intermediate waypoints exist on the line x = 100
		final Waypoint way1 = makeWaypoint(100, -20);
		final Waypoint way2 = makeWaypoint(100, -10);
		final Waypoint way3 = makeWaypoint(100, 0);
		final Waypoint way4 = makeWaypoint(100, 10);
		final Waypoint way5 = makeWaypoint(100, 20);

		final Waypoint destination = makeWaypoint(200, 0);

		// Quickest route is through way3
		final RefuelPathCalculator calculator = new RefuelPathCalculator();
		calculator.loadRefuelWaypoints(List.of(way1, way2, way3, way4, way5));

		final Ship ship = mock(Ship.class, Answers.RETURNS_DEEP_STUBS);
		final RoutePoint routePoint = mock();
		when(routePoint.getX()).thenReturn(0);
		when(routePoint.getY()).thenReturn(0);
		when(ship.getNav().getRoute().getDestination()).thenReturn(routePoint);

		final FuelStatus fuel = new FuelStatus(150, 150);
		when(ship.getFuel()).thenReturn(fuel);

		// Enough fuel to go from origin to any of the intermediate waypoints, but not
		// right to the destination
		final NavigationPath path = calculator.determineShortestRoute(ship, destination);

		assertEquals(200, path.getLength(), 1e-9);
		assertEquals(List.of(way3, destination), path.getWaypoints());
	}

	/**
	 * Tests the case where there is not enough max fuel to reach the destination
	 */
	@Test
	public void notEnoughFuel() {

		final Waypoint origin = makeWaypoint(0, 0);

		// The intermediate waypoints exist on the line x = 100
		final Waypoint way1 = makeWaypoint(100, -20);
		final Waypoint way2 = makeWaypoint(100, -10);
		final Waypoint way3 = makeWaypoint(100, 0);
		final Waypoint way4 = makeWaypoint(100, 10);
		final Waypoint way5 = makeWaypoint(100, 20);

		// Destination is much farther away this time
		final Waypoint destination = makeWaypoint(2000, 0);

		// Quickest route is through way3
		final RefuelPathCalculator calculator = new RefuelPathCalculator();
		calculator.loadRefuelWaypoints(List.of(way1, way2, way3, way4, way5));

		// Enough fuel to reach the intermediate points, but not the destination
		final NavigationPath path = calculator.determineShortestRoute(origin, destination, 201, 201);

		assertNull(path);
	}

	/**
	 * Tests the case where there is not enough current fuel to reach anything
	 */
	@Test
	public void notEnoughFuelForFirstHop() {

		final Waypoint origin = makeWaypoint(0, 0);

		// The intermediate waypoints exist on the line x = 100
		final Waypoint way1 = makeWaypoint(100, -20);
		final Waypoint way2 = makeWaypoint(100, -10);
		final Waypoint way3 = makeWaypoint(100, 0);
		final Waypoint way4 = makeWaypoint(100, 10);
		final Waypoint way5 = makeWaypoint(100, 20);

		final Waypoint destination = makeWaypoint(200, 0);

		// Quickest route is through way3
		final RefuelPathCalculator calculator = new RefuelPathCalculator();
		calculator.loadRefuelWaypoints(List.of(way1, way2, way3, way4, way5));

		// Not enough current fuel to go anywhere, but would be enough if we could refuel
		final NavigationPath path = calculator.determineShortestRoute(origin, destination, 1, 200);

		assertNull(path);
	}

	/**
	 * Tests the case where the best route goes straight from the origin to the destination
	 */
	@Test
	public void straightShot() {

		final Waypoint origin = makeWaypoint(0, 0);

		// The intermediate waypoints exist on the line x = 100
		final Waypoint way1 = makeWaypoint(100, -20);
		final Waypoint way2 = makeWaypoint(100, -10);
		// Slightly offset this one, otherwise hitting it would be the same distance as the straight shot
		final Waypoint way3 = makeWaypoint(100, 1);
		final Waypoint way4 = makeWaypoint(100, 10);
		final Waypoint way5 = makeWaypoint(100, 20);

		final Waypoint destination = makeWaypoint(200, 0);

		final RefuelPathCalculator calculator = new RefuelPathCalculator();
		calculator.loadRefuelWaypoints(List.of(way1, way2, way3, way4, way5));

		// Has enough fuel to go right to the destination
		final NavigationPath path = calculator.determineShortestRoute(origin, destination, 201, 201);

		assertEquals(200, path.getLength(), 1e-9);
		assertEquals(List.of(destination), path.getWaypoints());
	}

	private Waypoint makeWaypoint(final int x, final int y) {
		final Waypoint way = new Waypoint();
		way.setX(x);
		way.setY(y);
		return way;
	}

}
