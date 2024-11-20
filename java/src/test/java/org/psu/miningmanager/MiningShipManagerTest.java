package org.psu.miningmanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.psu.miningmanager.dto.Cooldown;
import org.psu.miningmanager.dto.ExtractResponse;
import org.psu.miningmanager.dto.MiningShipJob;
import org.psu.miningmanager.dto.MiningShipJob.State;
import org.psu.miningmanager.dto.Survey;
import org.psu.miningmanager.dto.SurveyResponse;
import org.psu.spacetraders.api.ClientProducer;
import org.psu.spacetraders.api.MarketplaceRequester;
import org.psu.spacetraders.api.NavigationHelper;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.api.SurveyClient;
import org.psu.spacetraders.dto.Cargo;
import org.psu.spacetraders.dto.CargoItem;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipNavigation;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testutils.TestRequestThrottler;
import org.psu.testutils.TestUtils;
import org.psu.trademanager.MarketplaceManager;
import org.psu.websocket.WebsocketReporter;


/**
 * Tests for {@link MiningShipManager}
 */
public class MiningShipManagerTest {

	/**
	 * Tests createJob for a ship that isn't doing anything
	 */
	@Test
	public void createNewJob() {

		final String destination = "destination";
		final Ship ship = mock(Ship.class, Answers.RETURNS_DEEP_STUBS);
		when(ship.getNav().getWaypointSymbol()).thenReturn(destination);

		final Waypoint way = mock(Waypoint.class);
		final MiningSiteManager miningSiteManager = mock(MiningSiteManager.class);
		when(miningSiteManager.getMiningSite(destination)).thenReturn(null);
		when(miningSiteManager.getClosestMiningSite(ship)).thenReturn(Optional.of(TestUtils.makeQueue(way)));

		final MarketplaceManager marketManager = mock(MarketplaceManager.class);
		when(marketManager.getMarketInfoById(destination)).thenReturn(Optional.empty());

		final WebsocketReporter reporter = mock(WebsocketReporter.class);

		final MiningShipManager manager = new MiningShipManager(1, mock(ClientProducer.class), null, miningSiteManager,
				null, marketManager, null, reporter);

		final MiningShipJob job = manager.createJob(ship);

		assertEquals(ship, job.getShip());
		assertEquals(way, job.getExtractionPoint());
		assertEquals(State.NOT_STARTED, job.getState());
	}

	/**
	 * Tests createJob for a ship that is traveling to a market which buys its goods
	 */
	@Test
	public void createJobTravelingToMarket() {

		final String productSymbol = "eggs";
		final CargoItem item = new CargoItem(productSymbol, 2);
		final Product product = new Product(productSymbol);
		final Cargo cargo = new Cargo(10, 2, List.of(item));

		final String destination = "destination";
		final Instant arrival = Instant.ofEpochSecond(10);
		final Ship ship = mock(Ship.class, Answers.RETURNS_DEEP_STUBS);
		when(ship.getNav().getWaypointSymbol()).thenReturn(destination);
		when(ship.getNav().getRoute().getArrival()).thenReturn(arrival);
		when(ship.getCargo()).thenReturn(cargo);

		final Waypoint way = mock(Waypoint.class);
		final MiningSiteManager miningSiteManager = mock(MiningSiteManager.class);
		when(miningSiteManager.getMiningSite(destination)).thenReturn(null);

		final MarketInfo market = mock(MarketInfo.class);
		when(market.sellsProduct(product)).thenReturn(true);

		final MarketplaceManager marketManager = mock(MarketplaceManager.class);
		when(marketManager.getMarketInfoById(destination))
				.thenReturn(Optional.of(new SimpleEntry<Waypoint, MarketInfo>(way, market)));

		final WebsocketReporter reporter = mock(WebsocketReporter.class);

		final MiningShipManager manager = new MiningShipManager(1, mock(ClientProducer.class), null, miningSiteManager,
				null, marketManager, null, reporter);

		final MiningShipJob job = manager.createJob(ship);

		assertEquals(ship, job.getShip());
		assertNull(job.getExtractionPoint());
		assertEquals(arrival, job.getNextAction());
		assertEquals(State.TRAVELING_TO_MARKET, job.getState());
	}

	/**
	 * Tests createJob for a ship that is traveling to some waypoint which will not buy its goods
	 */
	@Test
	public void createJobTravelingToOtherWaypoint() {

		final String productSymbol = "eggs";
		final CargoItem item = new CargoItem(productSymbol, 2);
		final Product product = new Product(productSymbol);
		final Cargo cargo = new Cargo(10, 2, List.of(item));

		final String destination = "destination";
		final Instant arrival = Instant.ofEpochSecond(10);
		final Ship ship = mock(Ship.class, Answers.RETURNS_DEEP_STUBS);
		when(ship.getNav().getWaypointSymbol()).thenReturn(destination);
		when(ship.getNav().getRoute().getArrival()).thenReturn(arrival);
		when(ship.getCargo()).thenReturn(cargo);

		final Waypoint way = mock(Waypoint.class);
		final MiningSiteManager miningSiteManager = mock(MiningSiteManager.class);
		when(miningSiteManager.getMiningSite(destination)).thenReturn(null);
		when(miningSiteManager.getClosestMiningSite(ship)).thenReturn(Optional.of(TestUtils.makeQueue(way)));

		final MarketInfo market = mock(MarketInfo.class);
		// Does not sell anything in the ship's cargo
		when(market.sellsProduct(product)).thenReturn(false);

		final MarketplaceManager marketManager = mock(MarketplaceManager.class);
		when(marketManager.getMarketInfoById(destination))
				.thenReturn(Optional.of(new SimpleEntry<Waypoint, MarketInfo>(way, market)));

		final WebsocketReporter reporter = mock(WebsocketReporter.class);

		final MiningShipManager manager = new MiningShipManager(1, mock(ClientProducer.class), null, miningSiteManager,
				null, marketManager, null, reporter);

		final MiningShipJob job = manager.createJob(ship);

		assertEquals(ship, job.getShip());
		assertEquals(way, job.getExtractionPoint());
		assertEquals(State.NOT_STARTED, job.getState());
	}

	/**
	 * Tests createJob for a ship that is traveling to a mining site
	 */
	@Test
	public void createJobTravelingToMiningSite() {

		final String destination = "destination";
		final Ship ship = mock(Ship.class, Answers.RETURNS_DEEP_STUBS);
		when(ship.getNav().getWaypointSymbol()).thenReturn(destination);
		// Will arrive in the future
		final Instant arrivalTime = Instant.now().plus(Duration.ofSeconds(30));
		when(ship.getNav().getRoute().getArrival()).thenReturn(arrivalTime);

		final Waypoint miningSite = mock(Waypoint.class);

		final MiningSiteManager miningSiteManager = mock(MiningSiteManager.class);
		when(miningSiteManager.getMiningSite(destination)).thenReturn(miningSite);

		final WebsocketReporter reporter = mock(WebsocketReporter.class);

		final MiningShipManager manager = new MiningShipManager(1000, mock(ClientProducer.class), null,
				miningSiteManager, null, null, null, reporter);

		final MiningShipJob job = manager.createJob(ship);

		// Manager using a 1000 ms cooldown pad
		final Instant expectedArrivalTime = arrivalTime.plus(Duration.ofSeconds(1));

		assertEquals(ship, job.getShip());
		assertEquals(miningSite, job.getExtractionPoint());
		assertEquals(State.TRAVELING_TO_RESOURCE, job.getState());
		assertEquals(expectedArrivalTime, job.getNextAction());
	}

	/**
	 * Tests createJob for a ship that is at a mining site with room in its cargo
	 */
	@Test
	public void createJobAtMiningSiteWithCargoSpace() {

		final String destination = "destination";
		final Ship ship = mock(Ship.class, Answers.RETURNS_DEEP_STUBS);
		when(ship.getNav().getWaypointSymbol()).thenReturn(destination);
		// Already arrived
		final Instant arrivalTime = Instant.now().minus(Duration.ofSeconds(30));
		when(ship.getNav().getRoute().getArrival()).thenReturn(arrivalTime);
		when(ship.getRemainingCargo()).thenReturn(10);

		final Waypoint miningSite = mock(Waypoint.class);

		final MiningSiteManager miningSiteManager = mock(MiningSiteManager.class);
		when(miningSiteManager.getMiningSite(destination)).thenReturn(miningSite);

		final WebsocketReporter reporter = mock(WebsocketReporter.class);

		final MiningShipManager manager = new MiningShipManager(1, mock(ClientProducer.class), null, miningSiteManager,
				null, null, null, reporter);

		final MiningShipJob job = manager.createJob(ship);

		assertEquals(ship, job.getShip());
		assertEquals(miningSite, job.getExtractionPoint());
		assertEquals(State.TRAVELING_TO_RESOURCE, job.getState());
	}

	/**
	 * Tests createJob for a ship that is at a mining site with no room in its cargo
	 */
	@Test
	public void createJobAtMiningSiteWithoutCargoSpace() {

		final String destination = "destination";
		final Ship ship = mock(Ship.class, Answers.RETURNS_DEEP_STUBS);
		when(ship.getNav().getWaypointSymbol()).thenReturn(destination);
		// Already arrived
		final Instant arrivalTime = Instant.now().minus(Duration.ofSeconds(30));
		when(ship.getNav().getRoute().getArrival()).thenReturn(arrivalTime);
		when(ship.getRemainingCargo()).thenReturn(0);

		final Waypoint miningSite = mock(Waypoint.class);

		final MiningSiteManager miningSiteManager = mock(MiningSiteManager.class);
		when(miningSiteManager.getMiningSite(destination)).thenReturn(miningSite);

		final WebsocketReporter reporter = mock(WebsocketReporter.class);

		final MiningShipManager manager = new MiningShipManager(1, mock(ClientProducer.class), null, miningSiteManager,
				null, null, null, reporter);

		final MiningShipJob job = manager.createJob(ship);

		assertEquals(ship, job.getShip());
		assertEquals(miningSite, job.getExtractionPoint());
		assertEquals(State.EXTRACTING, job.getState());
	}

	/**
	 * Tests manageMiningShip when the job hasn't been started
	 */
	@Test
	public void manageMiningShipNotStarted() {

		final SurveyClient surveyClient = mock(SurveyClient.class);
		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationHelper navigationHelper = mock(NavigationHelper.class);
		final WebsocketReporter reporter = mock(WebsocketReporter.class);
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceSurveyClient()).thenReturn(surveyClient);

		final String shipId = "shippy";
		final Ship ship = mock(Ship.class);
		when(ship.getSymbol()).thenReturn(shipId);

		final String extractionSiteId = "waypoint";
		final Waypoint extractionSite = mock(Waypoint.class);
		when(extractionSite.getSymbol()).thenReturn(extractionSiteId);

		final Instant arrivalTime = Instant.ofEpochSecond(10);
		when(navigationHelper.navigate(ship, extractionSite)).thenReturn(arrivalTime);

		final MiningShipJob job = new MiningShipJob(ship, TestUtils.makeQueue(extractionSite));

		final MiningShipManager manager = new MiningShipManager(1, clientProducer, throttler, null, navigationHelper,
				null, null, reporter);

		final MiningShipJob nextJob = manager.manageMiningShip(job);

		assertEquals(State.TRAVELING_TO_RESOURCE, nextJob.getState());
		assertEquals(arrivalTime, nextJob.getNextAction());
	}

	/**
	 * Tests manageMiningShip when the ship is partway through its route to the extraction site
	 */
	@Test
	public void manageMiningShipTravelingToResource() {

		final SurveyClient surveyClient = mock();
		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationHelper navigationHelper = mock();
		final MarketplaceRequester marketRequester = mock();
		final WebsocketReporter reporter = mock();
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceSurveyClient()).thenReturn(surveyClient);

		final String extractionSiteId = "waypoint";
		final Waypoint extractionSite = mock(Waypoint.class);
		when(extractionSite.getSymbol()).thenReturn(extractionSiteId);

		final String intermediateSiteId = "waypoint";
		final Waypoint intermediateSite = mock(Waypoint.class);
		when(intermediateSite.getSymbol()).thenReturn(intermediateSiteId);

		final String shipId = "shippy";
		final ShipNavigation shipNav = mock();
		when(shipNav.getWaypointSymbol()).thenReturn("some other waypoint");
		final Ship ship = mock();
		when(ship.getSymbol()).thenReturn(shipId);
		when(ship.getNav()).thenReturn(shipNav);

		final Instant arrivalTime = Instant.ofEpochSecond(10);
		when(navigationHelper.navigate(ship, intermediateSite)).thenReturn(arrivalTime);

		final MiningShipJob job = new MiningShipJob(ship, TestUtils.makeQueue(intermediateSite, extractionSite));
		job.setState(State.TRAVELING_TO_RESOURCE);

		final MiningShipManager manager = new MiningShipManager(0, clientProducer, throttler, null, navigationHelper,
				null, marketRequester, reporter);

		final MiningShipJob nextJob = manager.manageMiningShip(job);

		assertEquals(State.TRAVELING_TO_RESOURCE, nextJob.getState());
		assertEquals(arrivalTime, nextJob.getNextAction());
		verify(navigationHelper).navigate(ship, intermediateSite);
		verify(marketRequester).refuel(ship);
	}

	/**
	 * Tests manageMiningShip when the ship has finished traveling to the resource
	 */
	@Test
	public void manageMiningShipAtResource() {

		final SurveyClient surveyClient = mock(SurveyClient.class);
		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationHelper navigationHelper = mock(NavigationHelper.class);
		final WebsocketReporter reporter = mock(WebsocketReporter.class);
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceSurveyClient()).thenReturn(surveyClient);

		final String extractionSiteId = "waypoint";
		final Waypoint extractionSite = mock(Waypoint.class);
		when(extractionSite.getSymbol()).thenReturn(extractionSiteId);

		final String shipId = "shippy";
		final ShipNavigation shipNav = mock();
		when(shipNav.getWaypointSymbol()).thenReturn(extractionSiteId);
		final Ship ship = mock();
		when(ship.getSymbol()).thenReturn(shipId);
		when(ship.getNav()).thenReturn(shipNav);

		final SurveyResponse surveyResponse = mock(SurveyResponse.class);
		final Survey survey = mock(Survey.class);
		when(surveyResponse.getSurveys()).thenReturn(List.of(survey));
		final int cooldownSeconds = 50;
		final Cooldown cooldown = new Cooldown(cooldownSeconds);
		when(surveyResponse.getCooldown()).thenReturn(cooldown);

		when(surveyClient.survey(shipId)).thenReturn(new DataWrapper<SurveyResponse>(surveyResponse, null));

		final MiningShipJob job = new MiningShipJob(ship, TestUtils.makeQueue(extractionSite));
		job.setState(State.TRAVELING_TO_RESOURCE);

		final MiningShipManager manager = new MiningShipManager(0, clientProducer, throttler, null, navigationHelper,
				null, null, reporter);

		final MiningShipJob nextJob = manager.manageMiningShip(job);

		assertEquals(State.SURVEYING, nextJob.getState());
		assertEquals(List.of(survey), job.getSurveys());

		// Cooldown will be 50 seconds in the future, to account for test runtime lets just asset that
		// its between 45 and 55 seconds in the future
		assertTrue(Duration.between(Instant.now(), nextJob.getNextAction()).compareTo(Duration.ofSeconds(45)) > 0);
		assertTrue(Duration.between(Instant.now(), nextJob.getNextAction()).compareTo(Duration.ofSeconds(55)) < 0);
	}

	/**
	 * Tests manageMiningShip when the ship has finished surveying
	 */
	@Test
	public void manageMiningShipSurveying() {

		final SurveyClient surveyClient = mock(SurveyClient.class);
		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationHelper navigationHelper = mock(NavigationHelper.class);
		final WebsocketReporter reporter = mock(WebsocketReporter.class);
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceSurveyClient()).thenReturn(surveyClient);

		final String shipId = "shippy";
		final Ship ship = mock(Ship.class);
		when(ship.getSymbol()).thenReturn(shipId);
		when(ship.getRemainingCargo()).thenReturn(8);

		final Survey survey = mock(Survey.class);
		// Won't expire for a while
		when(survey.getExpiration()).thenReturn(Instant.now().plus(Duration.ofDays(1)));

		final String extractionSiteId = "waypoint";
		final Waypoint extractionSite = mock(Waypoint.class);
		when(extractionSite.getSymbol()).thenReturn(extractionSiteId);

		final Cargo cargo = new Cargo(10, 2, List.of());
		final ExtractResponse extractResponse = mock(ExtractResponse.class);
		when(extractResponse.getCargo()).thenReturn(cargo);
		final int cooldownSeconds = 50;
		when(extractResponse.getCooldown()).thenReturn(new Cooldown(cooldownSeconds));
		when(surveyClient.extractSurvey(shipId, survey))
				.thenReturn(new DataWrapper<ExtractResponse>(extractResponse, null));

		final MiningShipJob job = new MiningShipJob(ship, TestUtils.makeQueue(extractionSite));
		job.setState(State.SURVEYING);
		job.setSurveys(List.of(survey));

		final MiningShipManager manager = new MiningShipManager(0, clientProducer, throttler, null, navigationHelper,
				null, null, reporter);

		final MiningShipJob nextJob = manager.manageMiningShip(job);

		assertEquals(State.EXTRACTING, nextJob.getState());
		verify(ship).setCargo(cargo);

		// Cooldown will be 50 seconds in the future, to account for test runtime lets just asset that
		// its between 45 and 55 seconds in the future
		assertTrue(Duration.between(Instant.now(), nextJob.getNextAction()).compareTo(Duration.ofSeconds(45)) > 0);
		assertTrue(Duration.between(Instant.now(), nextJob.getNextAction()).compareTo(Duration.ofSeconds(55)) < 0);
	}

	/**
	 * Tests manageMiningShip when the ship has an expired survey
	 */
	@Test
	public void manageMiningShipSurveyingExpiredSurvey() {

		final SurveyClient surveyClient = mock(SurveyClient.class);
		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationHelper navigationHelper = mock(NavigationHelper.class);
		final WebsocketReporter reporter = mock(WebsocketReporter.class);
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceSurveyClient()).thenReturn(surveyClient);

		final String shipId = "shippy";
		final Ship ship = mock(Ship.class);
		when(ship.getSymbol()).thenReturn(shipId);
		when(ship.getRemainingCargo()).thenReturn(8);

		final Survey survey = mock(Survey.class);
		// Expired a day ago, uh oh!
		when(survey.getExpiration()).thenReturn(Instant.now().plus(Duration.ofDays(-1)));

		final String extractionSiteId = "waypoint";
		final Waypoint extractionSite = mock(Waypoint.class);
		when(extractionSite.getSymbol()).thenReturn(extractionSiteId);

		final MiningShipJob job = new MiningShipJob(ship, TestUtils.makeQueue(extractionSite));
		job.setState(State.SURVEYING);
		job.setSurveys(List.of(survey));

		final MiningShipManager manager = new MiningShipManager(0, clientProducer, throttler, null, navigationHelper,
				null, null, reporter);

		final MiningShipJob nextJob = manager.manageMiningShip(job);

		assertEquals(State.TRAVELING_TO_RESOURCE, nextJob.getState());
		verifyNoInteractions(surveyClient);
	}

	/**
	 * Tests manageMiningShip when the ship has finished extracting
	 */
	@Test
	public void manageMiningShipExtracting() {

		final SurveyClient surveyClient = mock(SurveyClient.class);
		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationHelper navigationHelper = mock(NavigationHelper.class);
		final MarketplaceManager marketplaceManager = mock(MarketplaceManager.class);
		final WebsocketReporter reporter = mock(WebsocketReporter.class);
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceSurveyClient()).thenReturn(surveyClient);

		// Want to sell this first because there are more of them
		final CargoItem cargoItem1 = new CargoItem("eggs", 20);
		final Product product1 = new Product("eggs");
		final CargoItem cargoItem2 = new CargoItem("milk", 10);
		final Product product2 = new Product("milk");
		final Cargo cargo = new Cargo(10, 2, List.of(cargoItem1, cargoItem2));

		final String shipId = "shippy";
		final Ship ship = mock(Ship.class);
		when(ship.getSymbol()).thenReturn(shipId);
		when(ship.getCargo()).thenReturn(cargo);
		// No remaining cargo, will finish extracting
		when(ship.getRemainingCargo()).thenReturn(0);

		// Nobody buys product1
		when(marketplaceManager.getClosestTradingWaypointPath(ship, product1)).thenReturn(Optional.empty());

		final Waypoint intermediateWaypoint = mock();

		final Waypoint sellingWaypoint = mock(Waypoint.class);
		when(marketplaceManager.getClosestTradingWaypointPath(ship, product2))
				.thenReturn(Optional.of(TestUtils.makeQueue(intermediateWaypoint, sellingWaypoint)));
		when(ship.canTravelTo(sellingWaypoint)).thenReturn(true);

		final Instant arrivalTime = Instant.ofEpochSecond(100);
		when(navigationHelper.navigate(ship, intermediateWaypoint)).thenReturn(arrivalTime);

		final String extractionSiteId = "waypoint";
		final Waypoint extractionSite = mock(Waypoint.class);
		when(extractionSite.getSymbol()).thenReturn(extractionSiteId);

		final Survey survey = mock(Survey.class);
		// Won't expire for a while
		when(survey.getExpiration()).thenReturn(Instant.now().plus(Duration.ofDays(1)));

		final ExtractResponse extractResponse = mock(ExtractResponse.class);
		when(extractResponse.getCargo()).thenReturn(cargo);
		final int cooldownSeconds = 50;
		when(extractResponse.getCooldown()).thenReturn(new Cooldown(cooldownSeconds));
		when(surveyClient.extractSurvey(shipId, survey))
				.thenReturn(new DataWrapper<ExtractResponse>(extractResponse, null));

		final MiningShipJob job = new MiningShipJob(ship, TestUtils.makeQueue(extractionSite));
		job.setState(State.EXTRACTING);
		job.setSurveys(List.of(survey));

		final MiningShipManager manager = new MiningShipManager(0, clientProducer, throttler, null, navigationHelper,
				marketplaceManager, null, reporter);

		final MiningShipJob nextJob = manager.manageMiningShip(job);

		assertEquals(State.TRAVELING_TO_MARKET, nextJob.getState());
		assertEquals(sellingWaypoint, nextJob.getSellingPoint());
		assertEquals(arrivalTime, nextJob.getNextAction());

		// Already started travel to the intermediate waypoint, should just be the selling waypoint left
		final Queue<Waypoint> sellingPath = nextJob.getSellingPath();
		assertEquals(sellingWaypoint, sellingPath.poll());
		assertNull(sellingPath.poll());
	}

	/**
	 * Tests manageMiningShip when the ship is in the middle of traveling to market
	 */
	@Test
	public void manageMiningShipTravelingToMarket() {

		final SurveyClient surveyClient = mock(SurveyClient.class);
		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationHelper navigationHelper = mock(NavigationHelper.class);
		final MiningSiteManager miningSiteManager = mock(MiningSiteManager.class);
		final MarketplaceManager marketManager = mock(MarketplaceManager.class);
		final MarketplaceRequester marketRequester = mock(MarketplaceRequester.class);
		final WebsocketReporter reporter = mock(WebsocketReporter.class);
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceSurveyClient()).thenReturn(surveyClient);

		final Waypoint extractionSite = mock(Waypoint.class);

		final Waypoint intermediateWaypoint = mock();
		final Waypoint sellingWaypoint = mock();

		final String shipId = "shippy";
		final Ship ship = mock(Ship.class, Answers.RETURNS_DEEP_STUBS);
		when(ship.getSymbol()).thenReturn(shipId);
		when(ship.getNav().getWaypointSymbol()).thenReturn("some other waypoint");

		final Instant arrivalTime = Instant.ofEpochSecond(100);
		when(navigationHelper.navigate(ship, intermediateWaypoint)).thenReturn(arrivalTime);

		final MiningShipJob job = new MiningShipJob(ship, TestUtils.makeQueue(extractionSite));
		job.setState(State.TRAVELING_TO_MARKET);
		job.setSellingPoint(extractionSite);
		job.setSellingPath(TestUtils.makeQueue(intermediateWaypoint, sellingWaypoint));

		final MiningShipManager manager = new MiningShipManager(1, clientProducer, throttler, miningSiteManager,
				navigationHelper, marketManager, marketRequester, reporter);

		final MiningShipJob nextJob = manager.manageMiningShip(job);

		assertEquals(State.TRAVELING_TO_MARKET, nextJob.getState());
		assertEquals(arrivalTime, nextJob.getNextAction());
		verify(navigationHelper).navigate(ship, intermediateWaypoint);
		verify(marketRequester).refuel(ship);

		final Queue<Waypoint> sellingPath = nextJob.getSellingPath();
		assertEquals(sellingWaypoint, sellingPath.poll());
		assertNull(sellingPath.poll());
	}

	/**
	 * Tests manageMiningShip when the ship has finished traveling to market
	 */
	@Test
	public void manageMiningShipAtMarket() {

		final SurveyClient surveyClient = mock(SurveyClient.class);
		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationHelper navigationHelper = mock(NavigationHelper.class);
		final MiningSiteManager miningSiteManager = mock(MiningSiteManager.class);
		final MarketplaceManager marketManager = mock(MarketplaceManager.class);
		final MarketplaceRequester marketRequester = mock(MarketplaceRequester.class);
		final WebsocketReporter reporter = mock(WebsocketReporter.class);
		final ClientProducer clientProducer = mock();
		when(clientProducer.produceSurveyClient()).thenReturn(surveyClient);

		final String extractionSiteId = "waypoint";
		final Waypoint extractionSite = mock(Waypoint.class);
		when(extractionSite.getSymbol()).thenReturn(extractionSiteId);

		final String shipId = "shippy";
		final List<CargoItem> cargoItems = List.of(new CargoItem("cheese", 2));
		final Ship ship = mock(Ship.class, Answers.RETURNS_DEEP_STUBS);
		when(ship.getSymbol()).thenReturn(shipId);
		when(ship.getNav().getWaypointSymbol()).thenReturn(extractionSiteId);
		when(ship.getCargo().getInventory()).thenReturn(cargoItems);

		when(miningSiteManager.getMiningSite(extractionSiteId)).thenReturn(null);
		when(miningSiteManager.getClosestMiningSite(ship)).thenReturn(Optional.of(TestUtils.makeQueue(extractionSite)));

		final MarketInfo marketInfo = mock(MarketInfo.class);
		when(marketManager.getMarketInfoById(extractionSiteId)).thenReturn(Optional.empty());
		when(marketManager.updateMarketInfo(extractionSite)).thenReturn(marketInfo);

		final MiningShipJob job = new MiningShipJob(ship, TestUtils.makeQueue(extractionSite));
		job.setState(State.TRAVELING_TO_MARKET);
		job.setSellingPoint(extractionSite);

		final MiningShipManager manager = new MiningShipManager(1, clientProducer, throttler, miningSiteManager,
				navigationHelper, marketManager, marketRequester, reporter);

		final MiningShipJob nextJob = manager.manageMiningShip(job);

		verify(marketRequester).dockAndSellItems(ship, extractionSite, cargoItems);
		assertEquals(State.NOT_STARTED, nextJob.getState());
	}

}
