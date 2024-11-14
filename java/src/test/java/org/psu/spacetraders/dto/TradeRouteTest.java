package org.psu.spacetraders.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
	 * Tests the constructor
	 */
	@Test
	public void constructor() {
		final Waypoint importWaypoint = mock(Waypoint.class);
		final Waypoint exportWaypoint = mock(Waypoint.class);
		final List<Product> goods = List.of(mock(Product.class), mock(Product.class));

		final TradeRoute route = new TradeRoute(exportWaypoint, importWaypoint, goods);

		assertEquals(importWaypoint, route.getImportWaypoint());
		assertEquals(exportWaypoint, route.getExportWaypoint());
		assertEquals(goods, route.getGoods());
		assertFalse(route.isKnown());
		assertNull(route.getPurchasePrice());
	}

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

	/**
	 * Tests the equals and hashCode methods
	 */
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void equalsAndHashCode() {

		final Waypoint way1 = new Waypoint();
		way1.setSymbol("way1");
		final Waypoint way2 = new Waypoint();
		way2.setSymbol("way2");
		final Waypoint way3 = new Waypoint();
		way3.setSymbol("way3");

		final Product product = new Product("milk");

		final TradeRoute route1 = new TradeRoute(way1, way2, List.of(product));

		// Different export waypoint
		final TradeRoute route2 = new TradeRoute(way3, way2, List.of(product));
		assertFalse(route1.equals(route2));
		assertNotEquals(route1.hashCode(), route2.hashCode());

		// Different import waypoint
		final TradeRoute route3 = new TradeRoute(way1, way3, List.of(product));
		assertFalse(route1.equals(route3));
		assertNotEquals(route1.hashCode(), route3.hashCode());

		// Different products, should be equal
		final TradeRoute route4 = new TradeRoute(way1, way2, List.of());
		assertTrue(route1.equals(route4));
		assertEquals(route1.hashCode(), route4.hashCode());

		final Integer notARoute = 1;
		assertFalse(route1.equals(notARoute));
		assertNotEquals(route1.hashCode(), notARoute.hashCode());
	}

}
