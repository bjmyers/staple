package org.psu.trademanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.api.MarketplaceClient;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testutils.TestRequestThrottler;

/**
 * Tests for {@link MarketplaceManager}
 */
public class MarketplaceManagerTest {

	/**
	 * Tests the update and get methods for {@link MarketplaceManager
	 */
	@Test
	public void updateAndGetMarketInfo() {

		final RequestThrottler throttler = TestRequestThrottler.get();
		final MarketplaceClient marketClient = mock(MarketplaceClient.class);
		final MarketplaceManager manager = new MarketplaceManager(throttler, marketClient);

		final Waypoint way1 = mock(Waypoint.class);
		final Waypoint way2 = mock(Waypoint.class);

		final MarketInfo market1 = mock(MarketInfo.class);
		final MarketInfo market2 = mock(MarketInfo.class);

		// Put the first two waypoints into the manager
		final Map<Waypoint, MarketInfo> currentInfo = Map.of(way1, market1, way2, market2);
		manager.updateMarketData(currentInfo);

		assertEquals(market1, manager.getMarketInfo(way1));
		assertEquals(market2, manager.getMarketInfo(way2));

		// The manager will have to query the marketplace client for the third waypoint
		final String systemId = "system";
		final String waypointId = "way";
		final Waypoint way3 = mock(Waypoint.class);
		when(way3.getSystemSymbol()).thenReturn(systemId);
		when(way3.getSymbol()).thenReturn(waypointId);

		final MarketInfo market3 = mock(MarketInfo.class);
		when(marketClient.getMarketInfo(systemId, waypointId)).thenReturn(new DataWrapper<MarketInfo>(market3, null));

		assertEquals(market3, manager.getMarketInfo(way3));
		// Should not have to call the marketClient this time
		assertEquals(market3, manager.getMarketInfo(way3));
		verify(marketClient, times(1)).getMarketInfo(systemId, waypointId);

		final Map<Waypoint, MarketInfo> allData = manager.getAllMarketInfo();
		assertEquals(3, allData.size());
	}

}
