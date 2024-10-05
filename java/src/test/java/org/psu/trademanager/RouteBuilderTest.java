package org.psu.trademanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.trademanager.dto.TradeRoute;

/**
 * Tests {@link RouteBuilder}
 */
public class RouteBuilderTest {

	/**
	 * Tests {@link RouteBuilder#buildTradeRoutes}
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

		final RouteBuilder routeBuilder = new RouteBuilder(marketplaceManager);

		final List<TradeRoute> routes = routeBuilder.buildTradeRoutes();

		final TradeRoute expectedRoute1 = new TradeRoute(way1, way3, List.of(prod1, prod2));
		final TradeRoute expectedRoute2 = new TradeRoute(way3, way2, List.of(prod1));

		assertEquals(2, routes.size());
		assertTrue(routes.contains(expectedRoute1));
		assertTrue(routes.contains(expectedRoute2));
	}

}
