package org.psu.init;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.api.ClientProducer;
import org.psu.spacetraders.api.MarketplaceClient;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.api.WaypointsClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.DataWrapper.WrapperMetadata;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Trait;
import org.psu.spacetraders.dto.Trait.Type;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testutils.TestRequestThrottler;

/**
 * Tests for {@link SystemBuilder}
 */
public class SystemBuilderTest {

	/**
	 * Tests {@link SystemBuilder#getWaypoints}
	 */
	@Test
	public void getWaypoints() {

		// Data will come in two pages
		final int total = 12;
		final int limit = 10;
		final WrapperMetadata metaData = new WrapperMetadata(total, 0, limit);

		final List<Waypoint> waypointPage1 = List.of(mock(Waypoint.class), mock(Waypoint.class));
		final List<Waypoint> waypointPage2 = List.of(mock(Waypoint.class));
		final DataWrapper<List<Waypoint>> waypointResponse1 = new DataWrapper<>(waypointPage1, metaData);
		final DataWrapper<List<Waypoint>> waypointResponse2 = new DataWrapper<>(waypointPage2, metaData);

		final String systemId = "I'm a system";
		final WaypointsClient waypointsClient = mock(WaypointsClient.class);
		when(waypointsClient.getWaypoints(systemId, limit, 1)).thenReturn(waypointResponse1);
		when(waypointsClient.getWaypoints(systemId, limit, 2)).thenReturn(waypointResponse2);
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceWaypointsClient()).thenReturn(waypointsClient);

		final RequestThrottler throttler = TestRequestThrottler.get();
		final SystemBuilder builder = new SystemBuilder(limit, throttler, clientProducer);

		final List<Waypoint> waypoints = builder.gatherWaypoints(systemId);

		assertEquals(3, waypoints.size());
		assertTrue(waypoints.containsAll(waypointPage1));
		assertTrue(waypoints.containsAll(waypointPage2));
	}

	/**
	 * Tests {@link SystemBuilder#gatherMarketInfo}
	 */
	@Test
	public void gatherMarketInfo() {

		final String systemId = "System";

		final Trait nonMarketTrait1 = new Trait(Type.ASH_CLOUDS);
		final Trait nonMarketTrait2 = new Trait(Type.COMMON_METAL_DEPOSITS);
		final Trait marketTrait = new Trait(Type.MARKETPLACE);

		final String way1Id = "Way1";
		final Waypoint way1 = mock(Waypoint.class);
		when(way1.getSystemSymbol()).thenReturn(systemId);
		when(way1.getSymbol()).thenReturn(way1Id);
		when(way1.getTraits()).thenReturn(List.of(marketTrait, nonMarketTrait1));

		final String way2Id = "Way2";
		final Waypoint way2 = mock(Waypoint.class);
		when(way2.getSystemSymbol()).thenReturn(systemId);
		when(way2.getSymbol()).thenReturn(way2Id);
		when(way2.getTraits()).thenReturn(List.of(marketTrait, nonMarketTrait2));

		// Does not have a marketplace
		final String way3Id = "Way3";
		final Waypoint way3 = mock(Waypoint.class);
		when(way3.getSystemSymbol()).thenReturn(systemId);
		when(way3.getSymbol()).thenReturn(way3Id);
		when(way3.getTraits()).thenReturn(List.of(nonMarketTrait1, nonMarketTrait2));

		final MarketplaceClient client = mock(MarketplaceClient.class);

		final MarketInfo market1 = mock(MarketInfo.class);
		when(client.getMarketInfo(systemId, way1Id)).thenReturn(new DataWrapper<MarketInfo>(market1, null));
		final MarketInfo market2 = mock(MarketInfo.class);
		when(client.getMarketInfo(systemId, way2Id)).thenReturn(new DataWrapper<MarketInfo>(market2, null));
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceMarketplaceClient()).thenReturn(client);

		final RequestThrottler throttler = TestRequestThrottler.get();
		final SystemBuilder builder = new SystemBuilder(0, throttler, clientProducer);

		final Map<Waypoint, MarketInfo> result = builder.gatherMarketInfo(List.of(way1, way2, way3));

		assertEquals(2, result.size());
		final MarketInfo way1Info = result.get(way1);
		assertEquals(market1, way1Info);
		final MarketInfo way2Info = result.get(way2);
		assertEquals(market2, way2Info);
	}

}
