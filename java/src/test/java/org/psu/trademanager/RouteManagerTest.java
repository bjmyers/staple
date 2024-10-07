package org.psu.trademanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.dto.FuelStatus;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.trademanager.dto.TradeRoute;

/**
 * Tests {@link RouteManager}
 */
public class RouteManagerTest {

	/**
	 * Tests {@link RouteManager#buildTradeRoutes}
	 */
	@Test
	public void buildTradeRoutes() {

		final MarketInfo market1 = mock(MarketInfo.class);
		final MarketInfo market2 = mock(MarketInfo.class);
		final MarketInfo market3 = mock(MarketInfo.class);
		final Waypoint way1 = mock(Waypoint.class);
		final Waypoint way2 = mock(Waypoint.class);
		final Waypoint way3 = mock(Waypoint.class);

		final Product prod1 = new Product("Freedom");
		final Product prod2 = new Product("Democracy");

		// Valid trade routes from way1 to way3 and from way3 to way2
		when(market1.getPotentialExports(market2)).thenReturn(List.of());
		when(market1.getPotentialExports(market3)).thenReturn(List.of(prod1, prod2));
		when(market2.getPotentialExports(market1)).thenReturn(List.of());
		when(market2.getPotentialExports(market3)).thenReturn(List.of());
		when(market3.getPotentialExports(market1)).thenReturn(List.of());
		when(market3.getPotentialExports(market2)).thenReturn(List.of(prod1));

		final Map<Waypoint, MarketInfo> marketInfo = Map.of(way1, market1, way2, market2, way3, market3);

		final MarketplaceManager marketplaceManager = mock(MarketplaceManager.class);
		when(marketplaceManager.getAllMarketInfo()).thenReturn(marketInfo);

		final RouteManager routeManager = new RouteManager(marketplaceManager);

		routeManager.buildTradeRoutes();
		final List<TradeRoute> routes = routeManager.getTradeRoutes();

		final TradeRoute expectedRoute1 = new TradeRoute(way1, way3, List.of(prod1, prod2));
		final TradeRoute expectedRoute2 = new TradeRoute(way3, way2, List.of(prod1));

		assertEquals(2, routes.size());
		assertTrue(routes.contains(expectedRoute1));
		assertTrue(routes.contains(expectedRoute2));
	}

	@Test
	public void getClosestRoute() {

		final MarketInfo market1 = mock(MarketInfo.class);
		final MarketInfo market2 = mock(MarketInfo.class);
		final MarketInfo market3 = mock(MarketInfo.class);
		final Waypoint way1 = mock(Waypoint.class);
		final Waypoint way2 = mock(Waypoint.class);
		final Waypoint way3 = mock(Waypoint.class);

		final Product prod1 = new Product("Freedom");
		final Product prod2 = new Product("Democracy");

		// Valid trade routes from way1 to way3, from way3 to way2, and from way2 to way3
		when(market1.getPotentialExports(market2)).thenReturn(List.of());
		when(market1.getPotentialExports(market3)).thenReturn(List.of(prod1, prod2));
		when(market2.getPotentialExports(market1)).thenReturn(List.of());
		when(market2.getPotentialExports(market3)).thenReturn(List.of(prod2));
		when(market3.getPotentialExports(market1)).thenReturn(List.of());
		when(market3.getPotentialExports(market2)).thenReturn(List.of(prod1));

		final Map<Waypoint, MarketInfo> marketInfo = Map.of(way1, market1, way2, market2, way3, market3);

		final Ship ship = mock(Ship.class);

		when(way1.getX()).thenReturn(0);
		when(way2.getX()).thenReturn(0);
		when(way3.getX()).thenReturn(0);
		when(way1.getY()).thenReturn(10);
		when(way2.getY()).thenReturn(20);
		when(way3.getY()).thenReturn(30);

		// Route1 (way1 -> way3) total distance: 10 + 20 units = 30 units
		when(ship.distTo(way1)).thenReturn(10.0);

		// Route1 (way3 -> way2) total distance: 15 + 10 units = 25 units
		when(ship.distTo(way3)).thenReturn(15.0);

		// Route1 (way2 -> way3) total distance: 10 + 10 units = 35 units
		// This route will also be impossible because the ship has 34 units of fuel
		when(ship.distTo(way2)).thenReturn(25.0);

		when(ship.getFuel()).thenReturn(new FuelStatus(34, 34));

		final MarketplaceManager marketplaceManager = mock(MarketplaceManager.class);
		when(marketplaceManager.getAllMarketInfo()).thenReturn(marketInfo);

		final RouteManager routeManager = new RouteManager(marketplaceManager);

		final Optional<TradeRoute> closestRoute = routeManager.getClosestRoute(ship);

		assertTrue(closestRoute.isPresent());
		assertEquals(way3, closestRoute.get().getExportWaypoint());
		assertEquals(way2, closestRoute.get().getImportWaypoint());

		// Do the same action again because now the trade routes don't have to be lazy-loaded
		final Optional<TradeRoute> newClosestRoute = routeManager.getClosestRoute(ship);

		assertTrue(newClosestRoute.isPresent());
		assertEquals(way3, newClosestRoute.get().getExportWaypoint());
		assertEquals(way2, newClosestRoute.get().getImportWaypoint());
	}

}
