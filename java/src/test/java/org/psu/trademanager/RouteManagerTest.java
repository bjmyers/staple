package org.psu.trademanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.psu.init.RandomProvider;
import org.psu.spacetraders.dto.FuelStatus;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipNavigation;
import org.psu.spacetraders.dto.ShipRoute.RoutePoint;
import org.psu.spacetraders.dto.TradeGood;
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

		final RouteManager routeManager = new RouteManager(marketplaceManager, new RandomProvider());

		routeManager.buildTradeRoutes();
		final List<TradeRoute> routes = routeManager.getTradeRoutes();

		final TradeRoute expectedRoute1 = new TradeRoute(way1, way3, List.of(prod1, prod2));
		final TradeRoute expectedRoute2 = new TradeRoute(way3, way2, List.of(prod1));

		assertEquals(2, routes.size());
		assertTrue(routes.contains(expectedRoute1));
		assertTrue(routes.contains(expectedRoute2));
	}

	/**
	 * Tests getBestRoute when all routes are impossible
	 */
	@Test
	public void getBestRouteAllImpossible() {

		final MarketplaceManager marketplaceManager = mock(MarketplaceManager.class);
		// No markets, no routes
		when(marketplaceManager.getAllMarketInfo()).thenReturn(Map.of());

		final RouteManager routeManager = new RouteManager(marketplaceManager, new RandomProvider());

		final Ship ship = mock(Ship.class);
		final TradeRoute route = routeManager.getBestRoute(ship);

		assertNull(route);
	}

	/**
	 * Tests getBestRoute when all routes are unknown
	 */
	@Test
	public void getBestRouteAllUnknown() {

		final Product prod1 = new Product("milk");
		final Product prod2 = new Product("eggs");

		// All markets have null trade goods
		final MarketInfo market1 = new MarketInfo();
		market1.setExports(List.of(prod1));
		market1.setImports(List.of());
		market1.setExchange(List.of());

		final MarketInfo market2 = new MarketInfo();
		market2.setExports(List.of(prod2));
		market2.setImports(List.of());
		market2.setExchange(List.of());

		final MarketInfo market3 = new MarketInfo();
		market3.setExports(List.of());
		market3.setImports(List.of(prod1, prod2));
		market3.setExchange(List.of());

		// Way2 is closer to Way3 than Way1 is
		final Waypoint way1 = new Waypoint();
		way1.setSymbol("way1");
		way1.setX(0);
		way1.setY(0);
		final Waypoint way2 = new Waypoint();
		way2.setSymbol("way2");
		way2.setX(10);
		way2.setY(0);
		final Waypoint way3 = new Waypoint();
		way3.setSymbol("way3");
		way3.setX(20);
		way3.setY(0);

		// Ship is at way2
		final RoutePoint routePoint = new RoutePoint("RP", 10, 0);
		final ShipNavigation shipNav = mock(ShipNavigation.class, Answers.RETURNS_DEEP_STUBS);
		when(shipNav.getRoute().getDestination()).thenReturn(routePoint);
		final Ship ship = new Ship();
		ship.setNav(shipNav);

		final FuelStatus fuel = new FuelStatus(1000, 1000);
		ship.setFuel(fuel);

		final MarketplaceManager marketplaceManager = mock(MarketplaceManager.class);
		when(marketplaceManager.getAllMarketInfo()).thenReturn(Map.of(way1, market1, way2, market2, way3, market3));
		when(marketplaceManager.getMarketInfo(way1)).thenReturn(market1);
		when(marketplaceManager.getMarketInfo(way2)).thenReturn(market2);
		when(marketplaceManager.getMarketInfo(way3)).thenReturn(market3);

		final RouteManager routeManager = new RouteManager(marketplaceManager, new RandomProvider());

		final TradeRoute bestRoute = routeManager.getBestRoute(ship);

		// Expect best route to be the one from way2 to way3
		assertEquals(way2, bestRoute.getExportWaypoint());
		assertEquals(way3, bestRoute.getImportWaypoint());
		assertEquals(List.of(prod2), bestRoute.getGoods());
	}

	/**
	 * Tests getBestRoute when all routes are known
	 */
	@Test
	public void getBestRouteAllKnown() {

		final Product prod1 = new Product("milk");

		// Market2 is the closest, but market1 has the best profit margin
		final TradeGood tradeGood1 = new TradeGood(prod1.getSymbol(), 10, 10, 10);
		final MarketInfo market1 = new MarketInfo();
		market1.setExports(List.of(prod1));
		market1.setImports(List.of());
		market1.setExchange(List.of());
		market1.setTradeGoods(List.of(tradeGood1));

		final TradeGood tradeGood2 = new TradeGood(prod1.getSymbol(), 20, 20, 20);
		final MarketInfo market2 = new MarketInfo();
		market2.setExports(List.of(prod1));
		market2.setImports(List.of());
		market2.setExchange(List.of());
		market2.setTradeGoods(List.of(tradeGood2));

		final TradeGood tradeGood3 = new TradeGood(prod1.getSymbol(), 30, 30, 30);
		final MarketInfo market3 = new MarketInfo();
		market3.setExports(List.of());
		market3.setImports(List.of(prod1));
		market3.setExchange(List.of());
		market3.setTradeGoods(List.of(tradeGood3));

		// Way2 is closer to Way3 than Way1 is
		final Waypoint way1 = new Waypoint();
		way1.setSymbol("way1");
		way1.setX(0);
		way1.setY(0);
		final Waypoint way2 = new Waypoint();
		way2.setSymbol("way2");
		way2.setX(10);
		way2.setY(0);
		final Waypoint way3 = new Waypoint();
		way3.setSymbol("way3");
		way3.setX(20);
		way3.setY(0);

		// Ship is at way2
		final RoutePoint routePoint = new RoutePoint("RP", 10, 0);
		final ShipNavigation shipNav = mock(ShipNavigation.class, Answers.RETURNS_DEEP_STUBS);
		when(shipNav.getRoute().getDestination()).thenReturn(routePoint);
		final Ship ship = new Ship();
		ship.setNav(shipNav);

		final FuelStatus fuel = new FuelStatus(1000, 1000);
		ship.setFuel(fuel);

		final MarketplaceManager marketplaceManager = mock(MarketplaceManager.class);
		when(marketplaceManager.getAllMarketInfo()).thenReturn(Map.of(way1, market1, way2, market2, way3, market3));
		when(marketplaceManager.getMarketInfo(way1)).thenReturn(market1);
		when(marketplaceManager.getMarketInfo(way2)).thenReturn(market2);
		when(marketplaceManager.getMarketInfo(way3)).thenReturn(market3);

		final RouteManager routeManager = new RouteManager(marketplaceManager, new RandomProvider());

		final TradeRoute bestRoute = routeManager.getBestRoute(ship);

		// Expect best route to be the one from way1 to way3, which has better profit margins
		assertEquals(way1, bestRoute.getExportWaypoint());
		assertEquals(way3, bestRoute.getImportWaypoint());
		assertEquals(List.of(prod1), bestRoute.getGoods());
	}

	/**
	 * Tests getBestRoute when it is given a choice between the shortest and the most profitable route
	 */
	@Test
	public void getBestRouteChoice() {

		final Product prod1 = new Product("milk");
		final Product prod2 = new Product("cheese");

		// Market1 will get a 20 credit profit for prod1, and a 5 credit profit for prod2
		final TradeGood tradeGood1 = new TradeGood(prod1.getSymbol(), 10, 10, 10);
		final TradeGood tradeGood2 = new TradeGood(prod2.getSymbol(), 25, 25, 25);
		final MarketInfo market1 = new MarketInfo();
		market1.setExports(List.of(prod1, prod2));
		market1.setImports(List.of());
		market1.setExchange(List.of());
		market1.setTradeGoods(List.of(tradeGood1, tradeGood2));

		// Market2 will be the shortest route, it needs to be unknown, so no trade goods for you
		final MarketInfo market2 = new MarketInfo();
		market2.setExports(List.of(prod1, prod2));
		market2.setImports(List.of());
		market2.setExchange(List.of());

		// Thus, market1 has the better profit
		final TradeGood tradeGood3 = new TradeGood(prod1.getSymbol(), 30, 30, 30);
		final TradeGood tradeGood4 = new TradeGood(prod2.getSymbol(), 30, 30, 30);
		final MarketInfo market3 = new MarketInfo();
		market3.setExports(List.of());
		market3.setImports(List.of(prod1, prod2));
		market3.setExchange(List.of());
		market3.setTradeGoods(List.of(tradeGood3, tradeGood4));

		// Way2 is closer to Way3 than Way1 is
		final Waypoint way1 = new Waypoint();
		way1.setSymbol("way1");
		way1.setX(0);
		way1.setY(0);
		final Waypoint way2 = new Waypoint();
		way2.setSymbol("way2");
		way2.setX(10);
		way2.setY(0);
		final Waypoint way3 = new Waypoint();
		way3.setSymbol("way3");
		way3.setX(20);
		way3.setY(0);

		// Ship is at way2, so the route using way2 will be shorter
		final RoutePoint routePoint = new RoutePoint("RP", 10, 0);
		final ShipNavigation shipNav = mock(ShipNavigation.class, Answers.RETURNS_DEEP_STUBS);
		when(shipNav.getRoute().getDestination()).thenReturn(routePoint);
		final Ship ship = new Ship();
		ship.setNav(shipNav);

		final FuelStatus fuel = new FuelStatus(1000, 1000);
		ship.setFuel(fuel);

		final MarketplaceManager marketplaceManager = mock(MarketplaceManager.class);
		when(marketplaceManager.getAllMarketInfo()).thenReturn(Map.of(way1, market1, way2, market2, way3, market3));
		when(marketplaceManager.getMarketInfo(way1)).thenReturn(market1);
		when(marketplaceManager.getMarketInfo(way2)).thenReturn(market2);
		when(marketplaceManager.getMarketInfo(way3)).thenReturn(market3);

		final RandomProvider randomProvider = mock(RandomProvider.class);
		// Start by returning a very small double, this means we will go with the most profitable route
		when(randomProvider.nextDouble()).thenReturn(0.0);

		final RouteManager routeManager = new RouteManager(marketplaceManager, randomProvider);

		final TradeRoute bestRoute = routeManager.getBestRoute(ship);

		// Expect best route to be the one from way1 to way3, which has better profit margins
		assertEquals(way1, bestRoute.getExportWaypoint());
		assertEquals(way3, bestRoute.getImportWaypoint());
		assertEquals(List.of(prod1), bestRoute.getGoods());

		// Now, return a large double, this means we will go with the shortest route
		when(randomProvider.nextDouble()).thenReturn(1.0);

		final TradeRoute nextBestRoute = routeManager.getBestRoute(ship);

		assertEquals(way2, nextBestRoute.getExportWaypoint());
		assertEquals(way3, nextBestRoute.getImportWaypoint());
		assertEquals(List.of(prod1, prod2), nextBestRoute.getGoods());
	}

}
