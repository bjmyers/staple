package org.psu.trademanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.AbstractMap.SimpleEntry;
import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;

import org.junit.jupiter.api.Test;
import org.psu.navigation.NavigationPath;
import org.psu.navigation.RefuelPathCalculator;
import org.psu.spacetraders.api.MarketplaceClient;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testutils.TestRequestThrottler;
import org.psu.testutils.TestUtils;

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
		final MarketplaceClient marketClient = mock();
		final RefuelPathCalculator pathCalculator = mock();
		final MarketplaceManager manager = new MarketplaceManager(throttler, marketClient, pathCalculator);

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

	/**
	 * Tests {@link MarkerplaceManager#getMarketInfoById}
	 */
	@Test
	public void getMarketInfoById() {

		final String way1Id = "way1";
		final Waypoint way1 = mock(Waypoint.class);
		when(way1.getSymbol()).thenReturn(way1Id);
		final MarketInfo market1 = mock(MarketInfo.class);
		final String way2Id = "way2";
		final Waypoint way2 = mock(Waypoint.class);
		when(way2.getSymbol()).thenReturn(way2Id);
		final MarketInfo market2 = mock(MarketInfo.class);

		final MarketplaceManager manager = new MarketplaceManager(null, null, null);
		manager.updateMarketData(Map.of(way1, market1, way2, market2));

		final Entry<Waypoint, MarketInfo> expected1 = new SimpleEntry<Waypoint, MarketInfo>(way1, market1);
		final Entry<Waypoint, MarketInfo> expected2 = new SimpleEntry<Waypoint, MarketInfo>(way2, market2);
		assertEquals(expected1, manager.getMarketInfoById(way1Id).get());
		assertEquals(expected2, manager.getMarketInfoById(way2Id).get());
		assertTrue(manager.getMarketInfoById("other waypoint").isEmpty());
	}

	/**
	 * Tests {@link MarketplaceManager#getClosestTradingWaypoint}
	 */
	@Test
	public void getClosestImport() {

		final Waypoint way1 = mock();
		final MarketInfo market1 = mock();
		final Waypoint way2 = mock();
		final MarketInfo market2 = mock();
		final Waypoint way3 = mock();
		final MarketInfo market3 = mock();
		final Waypoint way4 = mock();
		final MarketInfo market4 = mock();

		final Ship ship = mock(Ship.class);
		final Product product = mock(Product.class);

		// Way1 is closest to the ship, but doesn't sell the product, way2 is the closest which does sell it
		// The ship cannot reach way4
		when(market1.sellsProduct(product)).thenReturn(false);
		when(market2.sellsProduct(product)).thenReturn(true);
		when(market3.sellsProduct(product)).thenReturn(true);
		when(market4.sellsProduct(product)).thenReturn(true);

		final RefuelPathCalculator pathCalculator = mock();
		when(pathCalculator.determineShortestRoute(ship, way1))
				.thenReturn(new NavigationPath(1.0, TestUtils.makeQueue(way1)));
		when(pathCalculator.determineShortestRoute(ship, way2))
				.thenReturn(new NavigationPath(2.0, TestUtils.makeQueue(way2)));
		when(pathCalculator.determineShortestRoute(ship, way3))
				.thenReturn(new NavigationPath(3.0, TestUtils.makeQueue(way3)));
		when(pathCalculator.determineShortestRoute(ship, way4)).thenReturn(null);

		final MarketplaceManager manager = new MarketplaceManager(null, null, pathCalculator);
		manager.updateMarketData(Map.of(way1, market1, way2, market2, way3, market3, way4, market4));

		final Optional<Deque<Waypoint>> closestImportPath = manager.getClosestTradingWaypointPath(ship, product);
		assertTrue(closestImportPath.isPresent());
		final Queue<Waypoint> path = closestImportPath.get();
		assertEquals(way2, path.poll());
		assertNull(path.poll());
	}

}
