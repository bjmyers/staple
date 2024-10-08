package org.psu.spacetraders.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.dto.Agent;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.RefuelResponse;
import org.psu.spacetraders.dto.TradeRequest;
import org.psu.spacetraders.dto.TradeResponse;

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
		final TradeRequest request = mock(TradeRequest.class);

		final Agent agent = mock(Agent.class);
		final TradeResponse response = mock(TradeResponse.class);
		when(response.getAgent()).thenReturn(agent);

		when(client.purchase(shipId, request)).thenReturn(new DataWrapper<TradeResponse>(response, null));

		final MarketplaceRequester requester = new MarketplaceRequester(client, manager);

		final TradeResponse actualResponse = requester.purchase(shipId, request);
		assertEquals(response, actualResponse);
		verify(manager).updateAgent(agent);
	}

	/**
	 * Tests {@link MarketplaceRequester#sell}
	 */
	@Test
	public void sell() {

		final MarketplaceClient client = mock(MarketplaceClient.class);
		final AccountManager manager = mock(AccountManager.class);

		final String shipId = "shippy the ship";
		final TradeRequest request = mock(TradeRequest.class);

		final Agent agent = mock(Agent.class);
		final TradeResponse response = mock(TradeResponse.class);
		when(response.getAgent()).thenReturn(agent);

		when(client.sell(shipId, request)).thenReturn(new DataWrapper<TradeResponse>(response, null));

		final MarketplaceRequester requester = new MarketplaceRequester(client, manager);

		final TradeResponse actualResponse = requester.sell(shipId, request);
		assertEquals(response, actualResponse);
		verify(manager).updateAgent(agent);
	}

	/**
	 * Tests {@link MarketplaceRequester#refuel}
	 */
	@Test
	public void refuel() {

		final MarketplaceClient client = mock(MarketplaceClient.class);
		final AccountManager manager = mock(AccountManager.class);

		final String shipId = "shippy the ship";

		final Agent agent = mock(Agent.class);
		final RefuelResponse response = mock(RefuelResponse.class);
		when(response.getAgent()).thenReturn(agent);

		when(client.refuel(shipId)).thenReturn(new DataWrapper<RefuelResponse>(response, null));

		final MarketplaceRequester requester = new MarketplaceRequester(client, manager);

		final RefuelResponse actualResponse = requester.refuel(shipId);
		assertEquals(response, actualResponse);
		verify(manager).updateAgent(agent);
	}

}
