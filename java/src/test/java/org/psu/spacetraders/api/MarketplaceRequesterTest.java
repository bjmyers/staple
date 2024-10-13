package org.psu.spacetraders.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.dto.Agent;
import org.psu.spacetraders.dto.Cargo;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.FuelStatus;
import org.psu.spacetraders.dto.RefuelResponse;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.TradeRequest;
import org.psu.spacetraders.dto.TradeResponse;
import org.psu.testutils.TestRequestThrottler;

/**
 * Tests for {@link MarketplaceRequester}
 */
public class MarketplaceRequesterTest {

	/**
	 * Tests {@link MarketplaceRequester#purchase}
	 */
	@Test
	public void purchase() {

		final MarketplaceClient client = mock(MarketplaceClient.class);
		final AccountManager manager = mock(AccountManager.class);

		final String shipId = "shippy the ship";
		final Ship ship = mock(Ship.class);
		when(ship.getSymbol()).thenReturn(shipId);
		final TradeRequest request = mock(TradeRequest.class);

		final Agent agent = mock(Agent.class);
		final TradeResponse response = mock(TradeResponse.class);
		final Cargo cargo = mock(Cargo.class);
		when(response.getAgent()).thenReturn(agent);
		when(response.getCargo()).thenReturn(cargo);

		when(client.purchase(shipId, request)).thenReturn(new DataWrapper<TradeResponse>(response, null));

		final RequestThrottler throttler = TestRequestThrottler.get();
		final MarketplaceRequester requester = new MarketplaceRequester(client, throttler, manager);

		final TradeResponse actualResponse = requester.purchase(ship, request);
		assertEquals(response, actualResponse);
		verify(manager).updateAgent(agent);
		verify(ship).setCargo(cargo);
	}

	/**
	 * Tests {@link MarketplaceRequester#sell}
	 */
	@Test
	public void sell() {

		final MarketplaceClient client = mock(MarketplaceClient.class);
		final AccountManager manager = mock(AccountManager.class);

		final String shipId = "shippy the ship";
		final Ship ship = mock(Ship.class);
		when(ship.getSymbol()).thenReturn(shipId);
		final TradeRequest request = mock(TradeRequest.class);

		final Agent agent = mock(Agent.class);
		final TradeResponse response = mock(TradeResponse.class);
		final Cargo cargo = mock(Cargo.class);
		when(response.getAgent()).thenReturn(agent);
		when(response.getCargo()).thenReturn(cargo);

		when(client.sell(shipId, request)).thenReturn(new DataWrapper<TradeResponse>(response, null));

		final RequestThrottler throttler = TestRequestThrottler.get();
		final MarketplaceRequester requester = new MarketplaceRequester(client, throttler, manager);

		final TradeResponse actualResponse = requester.sell(ship, request);
		assertEquals(response, actualResponse);
		verify(manager).updateAgent(agent);
		verify(ship).setCargo(cargo);
	}

	/**
	 * Tests {@link MarketplaceRequester#refuel}
	 */
	@Test
	public void refuel() {

		final MarketplaceClient client = mock(MarketplaceClient.class);
		final AccountManager manager = mock(AccountManager.class);

		final String shipId = "shippy the ship";
		final Ship ship = mock(Ship.class);
		when(ship.getSymbol()).thenReturn(shipId);

		final Agent agent = mock(Agent.class);
		final FuelStatus fuelStatus = new FuelStatus(0, 0);
		final RefuelResponse response = mock(RefuelResponse.class);
		when(response.getAgent()).thenReturn(agent);
		when(response.getFuel()).thenReturn(fuelStatus);

		when(client.refuel(shipId)).thenReturn(new DataWrapper<RefuelResponse>(response, null));

		final RequestThrottler throttler = TestRequestThrottler.get();
		final MarketplaceRequester requester = new MarketplaceRequester(client, throttler, manager);

		final RefuelResponse actualResponse = requester.refuel(ship);
		assertEquals(response, actualResponse);
		verify(manager).updateAgent(agent);
		verify(ship).setFuel(fuelStatus);
	}

}
