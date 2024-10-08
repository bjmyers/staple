package org.psu.trademanager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.api.AccountManager;
import org.psu.spacetraders.api.MarketplaceRequester;
import org.psu.spacetraders.api.NavigationClient;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.NavigationResponse;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipNavigation;
import org.psu.spacetraders.dto.ShipRoute;
import org.psu.spacetraders.dto.TradeRequest;
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

		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationClient navClient = mock(NavigationClient.class);
		final AccountManager accountManager = mock(AccountManager.class);
		final MarketplaceRequester marketRequester = mock(MarketplaceRequester.class);
		final MarketplaceManager marketManager = mock(MarketplaceManager.class);
		final RouteManager routeManager = mock(RouteManager.class);
		final TradeShipManager manager = new TradeShipManager(throttler, navClient, accountManager, marketRequester,
				marketManager, routeManager);

		final Ship ship = mock(Ship.class);

		final String way1Symbol = "way1";
		final Waypoint way1 = mock(Waypoint.class);
		when(way1.getSymbol()).thenReturn(way1Symbol);
		final TradeRoute tradeRoute = mock(TradeRoute.class);
		when(tradeRoute.getExportWaypoint()).thenReturn(way1);
		when(tradeRoute.getImportWaypoint()).thenReturn(way1);
		when(tradeRoute.getGoods()).thenReturn(List.of(new Product("eggs")));

		when(routeManager.getClosestRoute(ship)).thenReturn(Optional.of(tradeRoute));

		final TradeRequest tradeRequest = mock(TradeRequest.class);

		final int credits = 5000;
		when(accountManager.getCredits()).thenReturn(credits);

		final MarketInfo marketInfo = mock(MarketInfo.class);
		when(marketInfo.sellsProduct(Product.FUEL)).thenReturn(true);
		when(marketInfo.buildPurchaseRequest(any(), anyInt(), eq(credits))).thenReturn(List.of(tradeRequest));
		when(marketInfo.rebalanceTradeRequests(eq(List.of(tradeRequest)))).thenReturn(List.of(tradeRequest));

		when(marketManager.updateMarketInfo(way1)).thenReturn(marketInfo);
		when(marketManager.getMarketInfo(way1)).thenReturn(marketInfo);

		final ShipNavigation nav = mock(ShipNavigation.class);
		when(nav.getWaypointSymbol()).thenReturn("some other waypoint");
		when(ship.getNav()).thenReturn(nav);

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
		when(marketRequester.purchase(any(), same(tradeRequest))).thenReturn(tradeResponse);
		when(marketRequester.sell(any(), same(tradeRequest))).thenReturn(tradeResponse);

		manager.manageTradeShip(ship);
	}

}
