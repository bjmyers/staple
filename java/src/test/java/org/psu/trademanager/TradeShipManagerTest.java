package org.psu.trademanager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.api.MarketplaceClient;
import org.psu.spacetraders.api.NavigationClient;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.NavigationResponse;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipNavigation;
import org.psu.spacetraders.dto.ShipRoute;
import org.psu.spacetraders.dto.TradeResponse;
import org.psu.spacetraders.dto.Transaction;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testutils.TestRequestThrottler;
import org.psu.trademanager.dto.TradeRoute;


/**
 * Tests for {@link TradeShipManager}
 */
public class TradeShipManagerTest {

	/**
	 * Tests {@link TradeShipManager#manageTradeShip}
	 */
	@Test
	public void manageTradeShip() {

		final RouteBuilder routeBuilder = mock(RouteBuilder.class);
		final MarketplaceClient marketClient = mock(MarketplaceClient.class);
		final NavigationClient navClient = mock(NavigationClient.class);
		final RequestThrottler throttler = TestRequestThrottler.get();
		final TradeShipManager manager = new TradeShipManager(throttler, routeBuilder, marketClient, navClient);

		final Ship ship = mock(Ship.class);

		final String way1Symbol = "way1";
		final Waypoint way1 = mock(Waypoint.class);
		when(way1.getSymbol()).thenReturn(way1Symbol);
		final TradeRoute route1 = mock(TradeRoute.class);
		when(route1.isPossible(ship)).thenReturn(true);
		when(route1.getExportWaypoint()).thenReturn(way1);
		when(route1.getImportWaypoint()).thenReturn(way1);
		when(route1.getGoods()).thenReturn(List.of(new Product("eggs")));

		final String way2Symbol = "way2";
		final Waypoint way2 = mock(Waypoint.class);
		when(way2.getSymbol()).thenReturn(way2Symbol);
		final TradeRoute route2 = mock(TradeRoute.class);
		when(route2.isPossible(ship)).thenReturn(true);
		when(route2.getExportWaypoint()).thenReturn(way2);

		// Impossible Trade Route
		final TradeRoute route3 = mock(TradeRoute.class);
		when(route3.isPossible(ship)).thenReturn(false);

		// The ship is closer to waypoint1, so route1 will be chosen
		when(ship.distTo(way1)).thenReturn(1.0);
		when(ship.distTo(way2)).thenReturn(5.0);

		final MarketInfo marketInfo = mock(MarketInfo.class);
		final Map<Waypoint, MarketInfo> systemMarketInfo = Map.of(way1, marketInfo, way2, marketInfo);
		when(marketInfo.sellsProduct(Product.FUEL)).thenReturn(true);

		final ShipNavigation nav = mock(ShipNavigation.class);
		when(nav.getWaypointSymbol()).thenReturn("some other waypoint");
		when(ship.getNav()).thenReturn(nav);

		when(routeBuilder.buildTradeRoutes(systemMarketInfo)).thenReturn(List.of(route1, route2, route3));

		final NavigationResponse navResponse = mock(NavigationResponse.class);
		final ShipNavigation shipNavResponse = mock(ShipNavigation.class);
		final ShipRoute route = mock(ShipRoute.class);
		when(route.getArrival()).thenReturn(Instant.now());
		when(shipNavResponse.getRoute()).thenReturn(route);
		when(navResponse.getNav()).thenReturn(shipNavResponse);
		when(navClient.navigate(any(), any())).thenReturn(new DataWrapper<NavigationResponse>(navResponse, null));

		final Transaction transaction = mock(Transaction.class);
		final TradeResponse tradeResponse = mock(TradeResponse.class);
		when(tradeResponse.getTransaction()).thenReturn(transaction);
		when(marketClient.purchase(any(), any())).thenReturn(new DataWrapper<TradeResponse>(tradeResponse, null));
		when(marketClient.sell(any(), any())).thenReturn(new DataWrapper<TradeResponse>(tradeResponse, null));

		manager.manageTradeShip(systemMarketInfo, ship);
	}


	/**
	 * Tests {@link TradeShipManager#manageTradeShip} with no routes
	 */
	@Test
	public void manageTradeShipNoRoutes() {

		final RouteBuilder routeBuilder = mock(RouteBuilder.class);
		final MarketplaceClient marketClient = mock(MarketplaceClient.class);
		final NavigationClient navClient = mock(NavigationClient.class);
		final RequestThrottler throttler = TestRequestThrottler.get();
		final TradeShipManager manager = new TradeShipManager(throttler, routeBuilder, marketClient, navClient);

		when(routeBuilder.buildTradeRoutes(any())).thenReturn(List.of());

		manager.manageTradeShip(Map.of(), mock(Ship.class));

	}

}
