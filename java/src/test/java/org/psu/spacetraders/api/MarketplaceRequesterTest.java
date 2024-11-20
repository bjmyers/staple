package org.psu.spacetraders.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.dto.Agent;
import org.psu.spacetraders.dto.Cargo;
import org.psu.spacetraders.dto.CargoItem;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.FuelStatus;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.RefuelResponse;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.TradeRequest;
import org.psu.spacetraders.dto.TradeResponse;
import org.psu.spacetraders.dto.Transaction;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testutils.TestRequestThrottler;
import org.psu.trademanager.MarketplaceManager;
import org.psu.websocket.WebsocketReporter;

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
		final WebsocketReporter reporter = mock(WebsocketReporter.class);
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceMarketplaceClient()).thenReturn(client);

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
		final MarketplaceRequester requester = new MarketplaceRequester(0, clientProducer, throttler, manager, null,
				null, reporter);

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
		final WebsocketReporter reporter = mock(WebsocketReporter.class);
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceMarketplaceClient()).thenReturn(client);

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
		final MarketplaceRequester requester = new MarketplaceRequester(0, clientProducer, throttler, manager, null,
				null, reporter);

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
		final WebsocketReporter reporter = mock(WebsocketReporter.class);
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceMarketplaceClient()).thenReturn(client);

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
		final MarketplaceRequester requester = new MarketplaceRequester(0, clientProducer, throttler, manager, null,
				null, reporter);

		final RefuelResponse actualResponse = requester.refuel(ship);
		assertEquals(response, actualResponse);
		verify(manager).updateAgent(agent);
		verify(ship).setFuel(fuelStatus);
	}

	/**
	 * Tests dockAndSellItems when the marketplace sells fuel
	 */
	@Test
	public void dockAndSellItemsRefuel() {

		final MarketplaceClient client = mock(MarketplaceClient.class);
		final NavigationHelper navHelper = mock(NavigationHelper.class);
		final AccountManager accountManager = mock(AccountManager.class);
		final MarketplaceManager marketplaceManager = mock(MarketplaceManager.class);
		final WebsocketReporter reporter = mock(WebsocketReporter.class);
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceMarketplaceClient()).thenReturn(client);
		final RequestThrottler throttler = TestRequestThrottler.get();
		final MarketplaceRequester requester = new MarketplaceRequester(0, clientProducer, throttler, accountManager,
				marketplaceManager, navHelper, reporter);

		final List<CargoItem> cargoItems = List.of(new CargoItem("eggs", 1));
		final String shipId = "shippy";
		final Ship ship = mock(Ship.class);
		when(ship.getSymbol()).thenReturn(shipId);

		final MarketInfo marketInfo = mock(MarketInfo.class);
		final TradeRequest tradeRequest = mock(TradeRequest.class);
		when(marketInfo.buildSellRequests(cargoItems)).thenReturn(List.of(tradeRequest));

		final Waypoint waypoint = mock(Waypoint.class);
		when(marketplaceManager.updateMarketInfo(waypoint)).thenReturn(marketInfo);

		final int sellPrice = 500;
		final Transaction transaction = mock(Transaction.class);
		when(transaction.getTotalPrice()).thenReturn(sellPrice);
		final TradeResponse tradeResponse = mock(TradeResponse.class);
		when(tradeResponse.getTransaction()).thenReturn(transaction);
		when(client.sell(shipId, tradeRequest)).thenReturn(new DataWrapper<TradeResponse>(tradeResponse, null));

		when(marketInfo.sellsProduct(Product.FUEL)).thenReturn(true);

		final int refuelPrice = 100;
		final Transaction refuelTransaction = mock(Transaction.class);
		when(refuelTransaction.getTotalPrice()).thenReturn(refuelPrice);
		final RefuelResponse response = mock(RefuelResponse.class);
		when(response.getTransaction()).thenReturn(refuelTransaction);
		when(client.refuel(shipId)).thenReturn(new DataWrapper<RefuelResponse>(response, null));

		final int profit = requester.dockAndSellItems(ship, waypoint, cargoItems);

		verify(navHelper).dock(ship);
		verify(client).sell(shipId, tradeRequest);
		verify(client).refuel(shipId);
		assertEquals(sellPrice - refuelPrice, profit);
	}

	/**
	 * Tests dockAndSellItems when the marketplace doesn't sell fuel
	 */
	@Test
	public void dockAndSellItemsNoRefuel() {

		final MarketplaceClient client = mock(MarketplaceClient.class);
		final NavigationHelper navHelper = mock(NavigationHelper.class);
		final AccountManager accountManager = mock(AccountManager.class);
		final MarketplaceManager marketplaceManager = mock(MarketplaceManager.class);
		final WebsocketReporter reporter = mock(WebsocketReporter.class);
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceMarketplaceClient()).thenReturn(client);
		final RequestThrottler throttler = TestRequestThrottler.get();
		final MarketplaceRequester requester = new MarketplaceRequester(0, clientProducer, throttler, accountManager,
				marketplaceManager, navHelper, reporter);

		final List<CargoItem> cargoItems = List.of(new CargoItem("eggs", 1));
		final String shipId = "shippy";
		final Ship ship = mock(Ship.class);
		when(ship.getSymbol()).thenReturn(shipId);

		final MarketInfo marketInfo = mock(MarketInfo.class);
		final TradeRequest tradeRequest = mock(TradeRequest.class);
		when(marketInfo.buildSellRequests(cargoItems)).thenReturn(List.of(tradeRequest));

		final Waypoint waypoint = mock(Waypoint.class);
		when(marketplaceManager.updateMarketInfo(waypoint)).thenReturn(marketInfo);

		final int sellPrice = 100;
		final Transaction transaction = mock(Transaction.class);
		when(transaction.getTotalPrice()).thenReturn(sellPrice);
		final TradeResponse tradeResponse = mock(TradeResponse.class);
		when(tradeResponse.getTransaction()).thenReturn(transaction);
		when(client.sell(shipId, tradeRequest)).thenReturn(new DataWrapper<TradeResponse>(tradeResponse, null));

		// Doesn't sell fuel
		when(marketInfo.sellsProduct(Product.FUEL)).thenReturn(false);

		final int profit = requester.dockAndSellItems(ship, waypoint, cargoItems);

		verify(navHelper).dock(ship);
		verify(client).sell(shipId, tradeRequest);
		verify(client, times(0)).refuel(shipId);
		assertEquals(profit, sellPrice);
	}

}
