package org.psu.trademanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.psu.spacetraders.api.AccountManager;
import org.psu.spacetraders.api.MarketplaceRequester;
import org.psu.spacetraders.api.NavigationHelper;
import org.psu.spacetraders.dto.Cargo;
import org.psu.spacetraders.dto.CargoItem;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.RefuelResponse;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipNavigation;
import org.psu.spacetraders.dto.TradeRequest;
import org.psu.spacetraders.dto.TradeResponse;
import org.psu.spacetraders.dto.Transaction;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.trademanager.RouteManager.RouteResponse;
import org.psu.trademanager.dto.TradeRoute;
import org.psu.trademanager.dto.TradeShipJob;
import org.psu.trademanager.dto.TradeShipJob.State;
import org.psu.websocket.WebsocketReporter;


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

		final RouteManager routeManager = mock();
		final TradeRoute route = mock();
		final Waypoint way = mock();
		final Queue<Waypoint> ways = new LinkedList<>();
		ways.add(way);
		final RouteResponse routeResponse = new RouteResponse(route, ways);
		when(routeManager.getBestRoute(ship)).thenReturn(routeResponse);
		final WebsocketReporter reporter = mock();

		final TradeShipManager manager = new TradeShipManager(0, 0, null, null, null, null, routeManager, reporter);

		final TradeShipJob job = manager.createJob(ship);

		assertEquals(ship, job.getShip());
		assertEquals(route, job.getRoute());
		assertEquals(List.of(way), job.getWaypoints());
		assertEquals(State.NOT_STARTED, job.getState());
	}

	/**
	 * Tests {@link TradeShipManager#createJob} when the ship's goods can be sold at its destination
	 */
	@Test
	public void createJobSellAtDestination() {

		final MarketplaceManager marketManager = mock(MarketplaceManager.class);
		final WebsocketReporter reporter = mock(WebsocketReporter.class);
		final TradeShipManager manager = new TradeShipManager(0, 0, null, null, null, marketManager, null, reporter);

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
		assertEquals(State.TRAVELING, job.getState());
		assertEquals(ship, job.getShip());
		assertEquals(way, job.getRoute().getImportWaypoint());
		assertEquals(arrivalTime, job.getNextAction());
		assertEquals(List.of(way), job.getWaypoints());

		final List<Product> products = job.getRoute().getGoods();
		assertEquals(2, products.size());
		assertTrue(products.contains(new Product(product1)));
		assertTrue(products.contains(new Product(product2)));
	}

	/**
	 * Tests {@link TradeShipManager#manageTradeShip} with a job that hasn't yet been started
	 */
	@Test
	public void manageTradeShipNotStarted() {

		final NavigationHelper navHelper = mock(NavigationHelper.class);
		final AccountManager accountManager = mock(AccountManager.class);
		final MarketplaceRequester marketRequester = mock(MarketplaceRequester.class);
		final MarketplaceManager marketManager = mock(MarketplaceManager.class);
		final RouteManager routeManager = mock(RouteManager.class);
		final WebsocketReporter reporter = mock(WebsocketReporter.class);
		final TradeShipManager manager = new TradeShipManager(0, 0, navHelper, accountManager, marketRequester,
				marketManager, routeManager, reporter);

		final Ship ship = mock(Ship.class);
		final ShipNavigation shipNav = mock(ShipNavigation.class);
		when(shipNav.getWaypointSymbol()).thenReturn("Starting Symbol");
		when(ship.getNav()).thenReturn(shipNav);

		final Waypoint intermediateWaypoint = mock();

		final Waypoint exportWaypoint = mock(Waypoint.class);
		when(exportWaypoint.getSymbol()).thenReturn("export waypoint");
		final TradeRoute tradeRoute = mock(TradeRoute.class);
		when(tradeRoute.getExportWaypoint()).thenReturn(exportWaypoint);

		final Instant arrivalTime = Instant.now().plus(Duration.ofMillis(10));
		when(navHelper.navigate(ship, intermediateWaypoint)).thenReturn(arrivalTime);

		final Queue<Waypoint> exports = new LinkedList<>();
		exports.add(intermediateWaypoint);
		exports.add(exportWaypoint);

		final TradeShipJob job = new TradeShipJob(ship, tradeRoute, exports);

		final TradeShipJob outputJob = manager.manageTradeShip(job);

		assertEquals(State.TRAVELING, outputJob.getState());
		assertEquals(arrivalTime, outputJob.getNextAction());
	}

	/**
	 * Tests {@link TradeShipManager#manageTradeShip} with a job where the ship has
	 * traveled to the export waypoint
	 */
	@Test
	public void manageTradeShipTravelingToExport() {

		final NavigationHelper navHelper = mock(NavigationHelper.class);
		final AccountManager accountManager = mock(AccountManager.class);
		final MarketplaceRequester marketRequester = mock(MarketplaceRequester.class);
		final MarketplaceManager marketManager = mock(MarketplaceManager.class);
		final RouteManager routeManager = mock(RouteManager.class);
		final WebsocketReporter reporter = mock(WebsocketReporter.class);
		final TradeShipManager manager = new TradeShipManager(0, 0, navHelper, accountManager, marketRequester,
				marketManager, routeManager, reporter);

		final String exportWaypointSymbol = "export";
		final Ship ship = mock(Ship.class);
		final ShipNavigation shipNav = mock(ShipNavigation.class);
		when(shipNav.getWaypointSymbol()).thenReturn(exportWaypointSymbol);
		when(ship.getNav()).thenReturn(shipNav);

		final Waypoint exportWaypoint = mock(Waypoint.class);
		final Waypoint importWaypoint = mock(Waypoint.class);
		when(exportWaypoint.getSymbol()).thenReturn(exportWaypointSymbol);
		when(importWaypoint.getSymbol()).thenReturn("import waypoint");
		final TradeRoute tradeRoute = mock(TradeRoute.class);
		when(tradeRoute.getExportWaypoint()).thenReturn(exportWaypoint);
		when(tradeRoute.getImportWaypoint()).thenReturn(importWaypoint);
		when(tradeRoute.isKnown()).thenReturn(false);

		final Instant arrivalTime = Instant.now().plus(Duration.ofMillis(10));
		when(navHelper.navigate(ship, importWaypoint)).thenReturn(arrivalTime);

		final int credits = 5000;
		when(accountManager.getCredits()).thenReturn(credits);

		final TradeRequest tradeRequest = mock(TradeRequest.class);
		final MarketInfo marketInfo = mock(MarketInfo.class);
		when(marketInfo.buildPurchaseRequest(any(), anyInt(), eq(credits), eq(false)))
				.thenReturn(List.of(tradeRequest));

		when(marketManager.updateMarketInfo(exportWaypoint)).thenReturn(marketInfo);

		final Transaction transaction = mock(Transaction.class);
		when(transaction.getTotalPrice()).thenReturn(100);
		final TradeResponse tradeResponse = mock(TradeResponse.class);
		when(tradeResponse.getTransaction()).thenReturn(transaction);
		when(marketRequester.purchase(any(), same(tradeRequest))).thenReturn(tradeResponse);

		final Transaction refuelTransaction = mock();
		when(refuelTransaction.getTotalPrice()).thenReturn(10);
		final RefuelResponse refuelResponse = mock();
		when(refuelResponse.getTransaction()).thenReturn(refuelTransaction);
		when(marketRequester.refuel(ship)).thenReturn(refuelResponse);

		final Queue<Waypoint> imports = new LinkedList<>();
		imports.add(importWaypoint);

		final TradeShipJob job = new TradeShipJob(ship, tradeRoute, imports);
		job.setState(State.TRAVELING);

		final TradeShipJob outputJob = manager.manageTradeShip(job);

		assertEquals(State.TRAVELING, outputJob.getState());
		assertEquals(arrivalTime, outputJob.getNextAction());
		// 100 credits for the purchase, 10 for refueling
		assertEquals(-110, outputJob.getProfit());
	}

	/**
	 * Tests {@link TradeShipManager#manageTradeShip} with a job where the ship has
	 * traveled to the import waypoint
	 */
	@Test
	public void manageTradeShipTravelingToImport() {

		final NavigationHelper navHelper = mock(NavigationHelper.class);
		final AccountManager accountManager = mock(AccountManager.class);
		final MarketplaceRequester marketRequester = mock(MarketplaceRequester.class);
		final MarketplaceManager marketManager = mock(MarketplaceManager.class);
		final RouteManager routeManager = mock(RouteManager.class);
		final WebsocketReporter reporter = mock(WebsocketReporter.class);
		final TradeShipManager manager = new TradeShipManager(0, 0, navHelper, accountManager, marketRequester,
				marketManager, routeManager, reporter);

		final String importWaypointSymbol = "import";
		final String productName = "product";
		final Ship ship = mock(Ship.class);
		final ShipNavigation shipNav = mock(ShipNavigation.class);
		when(shipNav.getWaypointSymbol()).thenReturn(importWaypointSymbol);
		when(ship.getNav()).thenReturn(shipNav);
		final CargoItem cargoItem = new CargoItem(productName, 1);
		when(ship.getCargo()).thenReturn(new Cargo(10, 0, List.of(cargoItem)));

		final Waypoint importWaypoint = mock(Waypoint.class);
		when(importWaypoint.getSymbol()).thenReturn(importWaypointSymbol);
		final TradeRoute tradeRoute = mock(TradeRoute.class);
		when(tradeRoute.getImportWaypoint()).thenReturn(importWaypoint);
		when(tradeRoute.getGoods()).thenReturn(List.of(new Product(productName)));

		final MarketInfo marketInfo = mock(MarketInfo.class);
		when(marketManager.updateMarketInfo(importWaypoint)).thenReturn(marketInfo);

		final TradeShipJob job = new TradeShipJob(ship, tradeRoute, new LinkedList<>());
		job.setState(State.TRAVELING);

		when(marketRequester.dockAndSellItems(ship, importWaypoint, List.of(cargoItem))).thenReturn(10);

		final TradeShipJob outputJob = manager.manageTradeShip(job);

		verify(marketRequester).dockAndSellItems(ship, importWaypoint, List.of(cargoItem));
		assertNull(outputJob);
	}

}
