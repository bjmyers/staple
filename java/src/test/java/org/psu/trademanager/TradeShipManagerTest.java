package org.psu.trademanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.psu.spacetraders.api.AccountManager;
import org.psu.spacetraders.api.MarketplaceRequester;
import org.psu.spacetraders.api.NavigationClient;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.dto.Cargo;
import org.psu.spacetraders.dto.CargoItem;
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
import org.psu.trademanager.dto.TradeShipJob;
import org.psu.trademanager.dto.TradeShipJob.State;


/**
 * Tests for {@link TradeShipManager}
 */
public class TradeShipManagerTest {

	/**
	 * Tests {@link TradeShipManager#createJob} when there is no cargo
	 */
	@Test
	public void createJobNoCargo() {

		final Cargo cargo = new Cargo(10, 0, List.of());
		final Ship ship = mock(Ship.class);
		when(ship.getCargo()).thenReturn(cargo);

		final RouteManager routeManager = mock(RouteManager.class);
		final TradeRoute route = mock(TradeRoute.class);
		when(routeManager.getClosestRoute(ship)).thenReturn(Optional.of(route));

		final TradeShipManager manager = new TradeShipManager(0, null, null, null, null, null, routeManager);

		final TradeShipJob job = manager.createJob(ship);

		assertEquals(ship, job.getShip());
		assertEquals(route, job.getRoute());
	}

	/**
	 * Tests {@link TradeShipManager#createJob} when the ship's goods can be sold at its destination
	 */
	@Test
	public void createJobSellAtDestination() {

		final MarketplaceManager marketManager = mock(MarketplaceManager.class);
		final TradeShipManager manager = new TradeShipManager(0, null, null, null, null, marketManager, null);

		final String product1 = "milk";
		final int product1Quantity = 5;
		final String product2 = "eggs";
		final int product2Quantity = 10;
		// Product3 will not be imported at the destination
		final String product3 = "cheese";
		final int product3Quantity = 1;
		final CargoItem cargoItem1 = new CargoItem(product1, product1Quantity);
		final CargoItem cargoItem2 = new CargoItem(product2, product2Quantity);
		final CargoItem cargoItem3 = new CargoItem(product3, product3Quantity);
		final Cargo cargo = new Cargo(50, 15, List.of(cargoItem1, cargoItem2, cargoItem3));

		final Ship ship = mock(Ship.class, Answers.RETURNS_DEEP_STUBS);
		when(ship.getCargo()).thenReturn(cargo);

		final String destWaypointId = "grocery store";
		final Waypoint way = mock(Waypoint.class);
		final MarketInfo market = mock(MarketInfo.class);
		when(market.getImports()).thenReturn(List.of(new Product(product1), new Product(product2)));
		when(marketManager.getMarketInfoById(destWaypointId))
				.thenReturn(Optional.of(new SimpleEntry<Waypoint, MarketInfo>(way, market)));

		final Instant arrivalTime = Instant.now();
		when(ship.getNav().getRoute().getDestination().getSymbol()).thenReturn(destWaypointId);
		when(ship.getNav().getRoute().getArrival()).thenReturn(arrivalTime);

		final TradeShipJob job = manager.createJob(ship);
		assertEquals(State.TRAVELING_TO_IMPORT, job.getState());
		assertEquals(ship, job.getShip());
		assertEquals(way, job.getRoute().getImportWaypoint());
		assertEquals(arrivalTime, job.getNextAction());

		final List<TradeRequest> purchases = job.getPurchases();
		assertEquals(2, purchases.size());
		assertTrue(purchases.contains(new TradeRequest(product1, product1Quantity)));
		assertTrue(purchases.contains(new TradeRequest(product2, product2Quantity)));
	}

	/**
	 * Tests {@link TradeShipManager#manageTradeShip} with a job that hasn't yet been started
	 */
	@Test
	public void manageTradeShipNotStarted() {

		final int navPadMs = 250;
		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationClient navClient = mock(NavigationClient.class);
		final AccountManager accountManager = mock(AccountManager.class);
		final MarketplaceRequester marketRequester = mock(MarketplaceRequester.class);
		final MarketplaceManager marketManager = mock(MarketplaceManager.class);
		final RouteManager routeManager = mock(RouteManager.class);
		final TradeShipManager manager = new TradeShipManager(navPadMs, throttler, navClient, accountManager,
				marketRequester, marketManager, routeManager);

		final NavigationResponse navResponse = mock(NavigationResponse.class);
		final ShipNavigation shipNavResponse = mock(ShipNavigation.class);
		final ShipRoute route = mock(ShipRoute.class);
		final Instant arrivalTime = Instant.now().plus(Duration.ofMillis(10));
		when(route.getArrival()).thenReturn(arrivalTime);
		when(shipNavResponse.getRoute()).thenReturn(route);
		when(navResponse.getNav()).thenReturn(shipNavResponse);
		when(navClient.navigate(any(), any())).thenReturn(new DataWrapper<NavigationResponse>(navResponse, null));

		final Ship ship = mock(Ship.class);
		final ShipNavigation shipNav = mock(ShipNavigation.class);
		when(shipNav.getWaypointSymbol()).thenReturn("Starting Symbol");
		when(ship.getNav()).thenReturn(shipNav);

		final Waypoint exportWaypoint = mock(Waypoint.class);
		when(exportWaypoint.getSymbol()).thenReturn("export waypoint");
		final TradeRoute tradeRoute = mock(TradeRoute.class);
		when(tradeRoute.getExportWaypoint()).thenReturn(exportWaypoint);

		final TradeShipJob job = new TradeShipJob(ship, tradeRoute);

		final TradeShipJob outputJob = manager.manageTradeShip(job);

		final Instant expectedArrivalTime = arrivalTime.plus(Duration.ofMillis(navPadMs));

		assertEquals(State.TRAVELING_TO_EXPORT, outputJob.getState());
		assertEquals(expectedArrivalTime, outputJob.getNextAction());
		verify(ship).setNav(shipNavResponse);
	}

	/**
	 * Tests {@link TradeShipManager#manageTradeShip} with a ship that is already at its export waypoint
	 */
	@Test
	public void manageTradeShipStartingAtExport() {

		final int navPadMs = 250;
		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationClient navClient = mock(NavigationClient.class);
		final AccountManager accountManager = mock(AccountManager.class);
		final MarketplaceRequester marketRequester = mock(MarketplaceRequester.class);
		final MarketplaceManager marketManager = mock(MarketplaceManager.class);
		final RouteManager routeManager = mock(RouteManager.class);
		final TradeShipManager manager = new TradeShipManager(navPadMs, throttler, navClient, accountManager,
				marketRequester, marketManager, routeManager);

		final String waypointSymbol = "waypoint";
		final Ship ship = mock(Ship.class);
		final ShipNavigation shipNav = mock(ShipNavigation.class);
		when(shipNav.getWaypointSymbol()).thenReturn(waypointSymbol);
		when(ship.getNav()).thenReturn(shipNav);

		final Waypoint exportWaypoint = mock(Waypoint.class);
		when(exportWaypoint.getSymbol()).thenReturn(waypointSymbol);
		final TradeRoute tradeRoute = mock(TradeRoute.class);
		when(tradeRoute.getExportWaypoint()).thenReturn(exportWaypoint);

		final TradeShipJob job = new TradeShipJob(ship, tradeRoute);

		final TradeShipJob outputJob = manager.manageTradeShip(job);

		assertEquals(State.TRAVELING_TO_EXPORT, outputJob.getState());
		verifyNoInteractions(navClient);
	}

	/**
	 * Tests {@link TradeShipManager#manageTradeShip} with a job where the ship was
	 * traveling to the export waypoint
	 */
	@Test
	public void manageTradeShipTravelingToExport() {

		final int navPadMs = 250;
		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationClient navClient = mock(NavigationClient.class);
		final AccountManager accountManager = mock(AccountManager.class);
		final MarketplaceRequester marketRequester = mock(MarketplaceRequester.class);
		final MarketplaceManager marketManager = mock(MarketplaceManager.class);
		final RouteManager routeManager = mock(RouteManager.class);
		final TradeShipManager manager = new TradeShipManager(navPadMs, throttler, navClient, accountManager,
				marketRequester, marketManager, routeManager);

		final NavigationResponse navResponse = mock(NavigationResponse.class);
		final ShipNavigation shipNavResponse = mock(ShipNavigation.class);
		final ShipRoute route = mock(ShipRoute.class);
		final Instant arrivalTime = Instant.now().plus(Duration.ofMillis(10));
		when(route.getArrival()).thenReturn(arrivalTime);
		when(shipNavResponse.getRoute()).thenReturn(route);
		when(navResponse.getNav()).thenReturn(shipNavResponse);
		when(navClient.navigate(any(), any())).thenReturn(new DataWrapper<NavigationResponse>(navResponse, null));

		final Ship ship = mock(Ship.class);
		final ShipNavigation shipNav = mock(ShipNavigation.class);
		when(shipNav.getWaypointSymbol()).thenReturn("Starting Symbol");
		when(ship.getNav()).thenReturn(shipNav);

		final Waypoint exportWaypoint = mock(Waypoint.class);
		final Waypoint importWaypoint = mock(Waypoint.class);
		when(importWaypoint.getSymbol()).thenReturn("import waypoint");
		final TradeRoute tradeRoute = mock(TradeRoute.class);
		when(tradeRoute.getExportWaypoint()).thenReturn(exportWaypoint);
		when(tradeRoute.getImportWaypoint()).thenReturn(importWaypoint);

		final int credits = 5000;
		when(accountManager.getCredits()).thenReturn(credits);

		final TradeRequest tradeRequest = mock(TradeRequest.class);
		final MarketInfo marketInfo = mock(MarketInfo.class);
		when(marketInfo.buildPurchaseRequest(any(), anyInt(), eq(credits))).thenReturn(List.of(tradeRequest));

		when(marketManager.updateMarketInfo(exportWaypoint)).thenReturn(marketInfo);

		final Transaction transaction = mock(Transaction.class);
		final TradeResponse tradeResponse = mock(TradeResponse.class);
		when(tradeResponse.getTransaction()).thenReturn(transaction);
		when(marketRequester.purchase(any(), same(tradeRequest))).thenReturn(tradeResponse);

		final TradeShipJob job = new TradeShipJob(ship, tradeRoute);
		job.setState(State.TRAVELING_TO_EXPORT);

		final TradeShipJob outputJob = manager.manageTradeShip(job);

		final Instant expectedArrivalTime = arrivalTime.plus(Duration.ofMillis(navPadMs));

		assertEquals(State.TRAVELING_TO_IMPORT, outputJob.getState());
		assertEquals(expectedArrivalTime, outputJob.getNextAction());
		verify(ship).setNav(shipNavResponse);
		assertEquals(List.of(tradeRequest), job.getPurchases());
	}

	/**
	 * Tests {@link TradeShipManager#manageTradeShip} with a job where the ship was
	 * traveling to the import waypoint
	 */
	@Test
	public void manageTradeShipTravelingToImport() {

		final int navPadMs = 250;
		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationClient navClient = mock(NavigationClient.class);
		final AccountManager accountManager = mock(AccountManager.class);
		final MarketplaceRequester marketRequester = mock(MarketplaceRequester.class);
		final MarketplaceManager marketManager = mock(MarketplaceManager.class);
		final RouteManager routeManager = mock(RouteManager.class);
		final TradeShipManager manager = new TradeShipManager(navPadMs, throttler, navClient, accountManager,
				marketRequester, marketManager, routeManager);

		final String shipId = "Ship";
		final Ship ship = mock(Ship.class);
		when(ship.getCargo()).thenReturn(new Cargo(10, 0, List.of()));
		when(ship.getSymbol()).thenReturn(shipId);
		final ShipNavigation shipNav = mock(ShipNavigation.class);
		when(shipNav.getWaypointSymbol()).thenReturn("Starting Symbol");
		when(ship.getNav()).thenReturn(shipNav);

		final Waypoint importWaypoint = mock(Waypoint.class);
		final TradeRoute tradeRoute = mock(TradeRoute.class);
		when(tradeRoute.getImportWaypoint()).thenReturn(importWaypoint);

		final int credits = 5000;
		when(accountManager.getCredits()).thenReturn(credits);

		final TradeRequest tradeRequest = mock(TradeRequest.class);
		final MarketInfo marketInfo = mock(MarketInfo.class);
		when(marketInfo.sellsProduct(Product.FUEL)).thenReturn(true);
		when(marketInfo.rebalanceTradeRequests(eq(List.of(tradeRequest)))).thenReturn(List.of(tradeRequest));
		when(marketManager.updateMarketInfo(importWaypoint)).thenReturn(marketInfo);
		when(marketManager.getMarketInfo(importWaypoint)).thenReturn(marketInfo);

		final Transaction transaction = mock(Transaction.class);
		final TradeResponse tradeResponse = mock(TradeResponse.class);
		when(tradeResponse.getTransaction()).thenReturn(transaction);
		when(marketRequester.sell(any(), same(tradeRequest))).thenReturn(tradeResponse);

		final TradeRoute newTradeRoute = mock(TradeRoute.class);
		when(routeManager.getClosestRoute(ship)).thenReturn(Optional.of(newTradeRoute));

		final TradeShipJob job = new TradeShipJob(ship, tradeRoute);
		job.setState(State.TRAVELING_TO_IMPORT);
		job.setPurchases(List.of(tradeRequest));

		final TradeShipJob outputJob = manager.manageTradeShip(job);

		assertEquals(State.NOT_STARTED, outputJob.getState());
		assertEquals(ship, outputJob.getShip());
		assertEquals(newTradeRoute, outputJob.getRoute());
		verify(marketRequester).refuel(shipId);
	}

	/**
	 * Tests {@link TradeShipManager#manageTradeShip} with a job where the ship was
	 * traveling to the import waypoint but the import waypoint doesn't sell fuel
	 */
	@Test
	public void manageTradeShipTravelingToImportNoFuel() {

		final int navPadMs = 250;
		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationClient navClient = mock(NavigationClient.class);
		final AccountManager accountManager = mock(AccountManager.class);
		final MarketplaceRequester marketRequester = mock(MarketplaceRequester.class);
		final MarketplaceManager marketManager = mock(MarketplaceManager.class);
		final RouteManager routeManager = mock(RouteManager.class);
		final TradeShipManager manager = new TradeShipManager(navPadMs, throttler, navClient, accountManager,
				marketRequester, marketManager, routeManager);

		final String shipId = "Ship";
		final Ship ship = mock(Ship.class);
		when(ship.getCargo()).thenReturn(new Cargo(10, 0, List.of()));
		when(ship.getSymbol()).thenReturn(shipId);
		final ShipNavigation shipNav = mock(ShipNavigation.class);
		when(shipNav.getWaypointSymbol()).thenReturn("Starting Symbol");
		when(ship.getNav()).thenReturn(shipNav);

		final Waypoint importWaypoint = mock(Waypoint.class);
		final TradeRoute tradeRoute = mock(TradeRoute.class);
		when(tradeRoute.getImportWaypoint()).thenReturn(importWaypoint);

		final int credits = 5000;
		when(accountManager.getCredits()).thenReturn(credits);

		final TradeRequest tradeRequest = mock(TradeRequest.class);
		final MarketInfo marketInfo = mock(MarketInfo.class);
		// No Fuel!
		when(marketInfo.sellsProduct(Product.FUEL)).thenReturn(false);
		when(marketInfo.rebalanceTradeRequests(eq(List.of(tradeRequest)))).thenReturn(List.of(tradeRequest));
		when(marketManager.updateMarketInfo(importWaypoint)).thenReturn(marketInfo);
		when(marketManager.getMarketInfo(importWaypoint)).thenReturn(marketInfo);

		final Transaction transaction = mock(Transaction.class);
		final TradeResponse tradeResponse = mock(TradeResponse.class);
		when(tradeResponse.getTransaction()).thenReturn(transaction);
		when(marketRequester.sell(any(), same(tradeRequest))).thenReturn(tradeResponse);

		final TradeRoute newTradeRoute = mock(TradeRoute.class);
		when(routeManager.getClosestRoute(ship)).thenReturn(Optional.of(newTradeRoute));

		final TradeShipJob job = new TradeShipJob(ship, tradeRoute);
		job.setState(State.TRAVELING_TO_IMPORT);
		job.setPurchases(List.of(tradeRequest));

		final TradeShipJob outputJob = manager.manageTradeShip(job);

		assertEquals(State.NOT_STARTED, outputJob.getState());
		assertEquals(ship, outputJob.getShip());
		assertEquals(newTradeRoute, outputJob.getRoute());
		verify(marketRequester, times(0)).refuel(shipId);
	}

}
