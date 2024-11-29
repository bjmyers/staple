package org.psu.shippurchase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.psu.shippurchase.ShipyardManager;
import org.psu.spacetraders.api.AccountManager;
import org.psu.spacetraders.api.ClientProducer;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.api.ShipyardClient;
import org.psu.spacetraders.dto.Agent;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipPurchaseRequest;
import org.psu.spacetraders.dto.ShipPurchaseResponse;
import org.psu.spacetraders.dto.ShipType;
import org.psu.spacetraders.dto.ShipyardResponse;
import org.psu.spacetraders.dto.ShipyardResponse.ShipTypeContainer;
import org.psu.spacetraders.dto.Trait;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testutils.TestRequestThrottler;

/**
 * Tests for {@link ShipyardManager}
 */
public class ShipyardManagerTest {

	/**
	 * Tests loadData
	 */
	@Test
	public void loadData() {

		final ShipyardClient shipyardClient = mock();
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceShipyardClient()).thenReturn(shipyardClient);
		final RequestThrottler requestThrottler = TestRequestThrottler.get();
		final AccountManager accountManager = mock();

		final String systemSymbol = "system";
		final String way1Id = "way1";
		final Waypoint way1 = mock();
		when(way1.getTraits()).thenReturn(List.of(Trait.SHIPYARD, Trait.MARKETPLACE));
		when(way1.getSystemSymbol()).thenReturn(systemSymbol);
		when(way1.getSymbol()).thenReturn(way1Id);

		final String way2Id = "way2";
		final Waypoint way2 = mock();
		when(way2.getTraits()).thenReturn(List.of());
		when(way2.getSystemSymbol()).thenReturn(systemSymbol);
		when(way2.getSymbol()).thenReturn(way2Id);

		final String way3Id = "way3";
		final Waypoint way3 = mock();
		when(way3.getTraits()).thenReturn(List.of(Trait.SHIPYARD));
		when(way3.getSystemSymbol()).thenReturn(systemSymbol);
		when(way3.getSymbol()).thenReturn(way3Id);

		final ShipyardResponse shipyardResponse1 = new ShipyardResponse(way1Id,
				List.of(new ShipTypeContainer(ShipType.SHIP_EXPLORER), new ShipTypeContainer(ShipType.SHIP_PROBE)));
		when(shipyardClient.getShipyardData(systemSymbol, way1Id)).thenReturn(new DataWrapper<>(shipyardResponse1, null));

		final ShipyardResponse shipyardResponse3 = new ShipyardResponse(way3Id,
				List.of(new ShipTypeContainer(ShipType.SHIP_INTERCEPTOR), new ShipTypeContainer(ShipType.SHIP_PROBE)));
		when(shipyardClient.getShipyardData(systemSymbol, way3Id)).thenReturn(new DataWrapper<>(shipyardResponse3, null));

		final ShipyardManager shipyardManager = new ShipyardManager(clientProducer, requestThrottler, accountManager);

		shipyardManager.loadData(List.of(way1, way2, way3));

		final List<Waypoint> probeWaypoints = shipyardManager.getShipyardsWhichSell(ShipType.SHIP_PROBE);
		assertEquals(2, probeWaypoints.size());
		assertTrue(probeWaypoints.contains(way1));
		assertTrue(probeWaypoints.contains(way3));

		final List<Waypoint> explorerWaypoints = shipyardManager.getShipyardsWhichSell(ShipType.SHIP_EXPLORER);
		assertEquals(1, explorerWaypoints.size());
		assertTrue(explorerWaypoints.contains(way1));
	}

	/**
	 * Tests purchaseShip
	 */
	@Test
	public void purchaseShipTest() {

		final ShipyardClient shipyardClient = mock();
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceShipyardClient()).thenReturn(shipyardClient);
		final RequestThrottler requestThrottler = TestRequestThrottler.get();
		final AccountManager accountManager = mock();

		final ShipPurchaseRequest request = mock();
		final Agent agent = mock();
		final ShipPurchaseResponse response = mock();
		when(response.getAgent()).thenReturn(agent);
		when(response.getShip()).thenReturn(mock(Ship.class));
		when(shipyardClient.purchaseShip(request)).thenReturn(new DataWrapper<>(response, null));

		final ShipyardManager shipyardManager = new ShipyardManager(clientProducer, requestThrottler, accountManager);

		final ShipPurchaseResponse actualResponse = shipyardManager.purchaseShip(request);

		assertEquals(response, actualResponse);
		verify(accountManager).updateAgent(agent);
	}

}
