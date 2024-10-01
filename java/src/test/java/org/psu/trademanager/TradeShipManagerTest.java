package org.psu.trademanager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.Waypoint;
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

		final Ship ship = mock(Ship.class);
		final Map<Waypoint, MarketInfo> systemMarketInfo = Map.of(mock(Waypoint.class), mock(MarketInfo.class));

		final Waypoint way1 = mock(Waypoint.class);
		final TradeRoute route1 = mock(TradeRoute.class);
		when(route1.getExportWaypoint()).thenReturn(way1);
		when(route1.getImportWaypoint()).thenReturn(way1);

		final Waypoint way2 = mock(Waypoint.class);
		final TradeRoute route2 = mock(TradeRoute.class);
		when(route2.getExportWaypoint()).thenReturn(way2);

		// The ship is closer to waypoint1, so route1 will be chosen
		when(ship.distTo(way1)).thenReturn(1.0);
		when(ship.distTo(way2)).thenReturn(5.0);

		final RouteBuilder routeBuilder = mock(RouteBuilder.class);
		when(routeBuilder.buildTradeRoutes(systemMarketInfo)).thenReturn(List.of(route1, route2));
		final TradeShipManager manager = new TradeShipManager(routeBuilder);

		manager.manageTradeShip(systemMarketInfo, ship);
	}

}
