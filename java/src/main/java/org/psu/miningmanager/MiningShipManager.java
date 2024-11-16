package org.psu.miningmanager;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.psu.miningmanager.dto.ExtractResponse;
import org.psu.miningmanager.dto.MiningShipJob;
import org.psu.miningmanager.dto.MiningShipJob.State;
import org.psu.miningmanager.dto.Survey;
import org.psu.miningmanager.dto.SurveyResponse;
import org.psu.spacetraders.api.MarketplaceRequester;
import org.psu.spacetraders.api.NavigationHelper;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.api.SurveyClient;
import org.psu.spacetraders.dto.CargoItem;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.trademanager.MarketplaceManager;
import org.psu.websocket.WebsocketReporter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Executes actions for mining ships
 */
@JBossLog
@ApplicationScoped
public class MiningShipManager {

	private Duration cooldownPad;
	private SurveyClient surveyClient;
	private RequestThrottler throttler;
	private MiningSiteManager siteManager;
	private NavigationHelper navHelper;
	private MarketplaceManager marketplaceManager;
	private MarketplaceRequester marketplaceRequester;
	private WebsocketReporter websocketReporter;

	@Inject
	public MiningShipManager(@ConfigProperty(name = "app.cooldown-pad-ms") final int cooldownPad,
			@RestClient final SurveyClient surveyClient, final RequestThrottler throttler,
			final MiningSiteManager siteManager, final NavigationHelper navHelper,
			final MarketplaceManager marketplaceManager, final MarketplaceRequester marketplaceRequester,
			final WebsocketReporter websocketReporter) {
		this.cooldownPad = Duration.ofMillis(cooldownPad);
		this.surveyClient = surveyClient;
		this.throttler = throttler;
		this.siteManager = siteManager;
		this.navHelper = navHelper;
		this.marketplaceManager = marketplaceManager;
		this.marketplaceRequester = marketplaceRequester;
		this.websocketReporter = websocketReporter;
	}

	public MiningShipJob createJob(final Ship ship) {

		final Waypoint destination = siteManager.getMiningSite(ship.getNav().getWaypointSymbol());
		if (destination != null) {
			final Deque<Waypoint> destinationPath = new LinkedList<>();
			destinationPath.add(destination);

			// The ship is at or traveling to a mining site
			if (ship.getNav().getRoute().getArrival().compareTo(Instant.now()) > 0) {
				// The ship will arrive in the future
				final MiningShipJob job = new MiningShipJob(ship, destinationPath);
				job.setState(State.TRAVELING_TO_RESOURCE);
				job.setNextAction(ship.getNav().getRoute().getArrival().plus(cooldownPad));
				return job;
			}
			// The ship is already at a mining site
			if (ship.getRemainingCargo() > 0) {
				// We're not full, start by surveying and then start extracting
				final MiningShipJob job = new MiningShipJob(ship, destinationPath);
				job.setState(State.TRAVELING_TO_RESOURCE);
				return job;
			}
			// We're full, let the job calculate the marketplace to sell
			final MiningShipJob job = new MiningShipJob(ship, destinationPath);
			job.setState(State.EXTRACTING);
			return job;
		}
		// The ship is not at a mining site or traveling to one.

		// Figure out if we're traveling to a marketplace that can buy our cargo
		final Optional<Entry<Waypoint, MarketInfo>> optionalDestinationMarketInfo = marketplaceManager
				.getMarketInfoById(ship.getNav().getWaypointSymbol());
		if (optionalDestinationMarketInfo.isPresent()) {
			final Entry<Waypoint, MarketInfo> destinationMarketInfo = optionalDestinationMarketInfo.get();

			if (ship.getCargo().inventory().stream().map(c -> new Product(c.symbol())).anyMatch(
					p -> destinationMarketInfo.getValue().sellsProduct(p))) {

				final Queue<Waypoint> marketPath = new LinkedList<>();
				marketPath.add(destinationMarketInfo.getKey());

				// The destination does indeed buy something that we have
				// Extraction point doesn't matter, we've already extracted everything
				final MiningShipJob job = new MiningShipJob(ship, new LinkedList<>());
				job.setState(State.TRAVELING_TO_MARKET);
				job.setNextAction(ship.getNav().getRoute().getArrival());
				job.setSellingPath(marketPath);
				job.setSellingPoint(destinationMarketInfo.getKey());
				return job;
			}
		}

		// We're not in the middle of a mining job, make a new one
		final Deque<Waypoint> extractionPath = siteManager.getClosestMiningSite(ship).get();
		return new MiningShipJob(ship, extractionPath);
	}

	public MiningShipJob manageMiningShip(final MiningShipJob job) {
		final Ship ship = job.getShip();
		final String shipId = ship.getSymbol();

		// The state of the job indicates what is was doing before it reached the manager
		switch (job.getState()) {
		case NOT_STARTED:
			final Waypoint nextWaypoint = job.getExtractionPath().remove();
			log.infof("Ship %s traveling to waypoint %s", shipId, nextWaypoint.getSymbol());
			final Instant extractionArrival = navHelper.navigate(ship, nextWaypoint);
			job.setNextAction(extractionArrival);
			job.setState(State.TRAVELING_TO_RESOURCE);
			break;
		case TRAVELING_TO_RESOURCE:

			if (ship.getNav().getWaypointSymbol().equals(job.getExtractionPoint().getSymbol())) {
				// We've reached the extraction point
				final SurveyResponse surveyResponse = throttler.throttle(() -> surveyClient.survey(shipId)).getData();
				job.setSurveys(surveyResponse.getSurveys());
				final Instant surveyCooldownComplete = Instant.now()
						.plus(Duration.ofSeconds(surveyResponse.getCooldown().getTotalSeconds()));
				job.setNextAction(surveyCooldownComplete);
				job.setState(State.SURVEYING);

				final String surveyMessage = String.format(
						"Finished Surveying, found %s sites, ship %s in cooldown until %s", job.getSurveys().size(), shipId,
						job.getNextAction());
				websocketReporter.fireShipEvent(ship.getSymbol(), surveyMessage);
				log.infof(surveyMessage);
				break;
			}

			// If we haven't reached the extraction point yet, continue with the path
			final Waypoint nextExtractionWaypoint = job.getExtractionPath().remove();
			log.infof("Ship %s traveling to waypoint %s", shipId, nextExtractionWaypoint);
			marketplaceRequester.refuel(ship);
			final Instant nextWaypointArrival = navHelper.navigate(ship, nextExtractionWaypoint);
			job.setNextAction(nextWaypointArrival);
			break;

		case SURVEYING, EXTRACTING:
			// If we're full, finish extracting
			if (ship.getRemainingCargo() == 0) {
				final Instant arrival = findAndNavigateToMarket(job);
				job.setState(State.TRAVELING_TO_MARKET);
				job.setNextAction(arrival);
				return job;
			}
			final Survey survey = job.getSurveys().get(0);
			if (survey.getExpiration().isBefore(Instant.now())) {
				// Survey has expired. Need to re-up the survey
				job.setState(State.TRAVELING_TO_RESOURCE);
				job.setNextAction(Instant.now());
				break;
			}
			final String message = String.format("Ship %s extracting resources", shipId);
			websocketReporter.fireShipEvent(ship.getSymbol(), message);
			log.infof(message);
			final ExtractResponse extractResponse = throttler.throttle(() -> surveyClient.extractSurvey(shipId, survey))
					.getData();
			ship.setCargo(extractResponse.getCargo());

			final Instant extractCooldownComplete = Instant.now()
					.plus(Duration.ofSeconds(extractResponse.getCooldown().getTotalSeconds()));

			job.setNextAction(extractCooldownComplete);
			job.setState(State.EXTRACTING);
			final String extractMessage = String.format("Ship %s finished extraction, ready to extract again at %s",
					shipId, job.getNextAction());
			websocketReporter.fireShipEvent(ship.getSymbol(), extractMessage);
			log.infof(extractMessage);
			break;
		case TRAVELING_TO_MARKET:

			if (ship.getNav().getWaypointSymbol().equals(job.getSellingPoint().getSymbol())) {
				// We've reached the market
				sellItems(job);
				// Make a whole new job and return it
				return createJob(job.getShip());
			}
			// We need to keep traveling along the path
			final Waypoint nextSellingPathPoint = job.getSellingPath().remove();
			log.infof("Ship %s traveling to waypoint %s", shipId, nextSellingPathPoint);
			marketplaceRequester.refuel(ship);
			final Instant nextSellingPathPointArrival = navHelper.navigate(ship, nextSellingPathPoint);
			job.setNextAction(nextSellingPathPointArrival);
			break;
		}
		return job;
	}

	private Instant findAndNavigateToMarket(final MiningShipJob job) {
		final Ship ship = job.getShip();

		// Sorted so that the item with the highest count is first
		final List<CargoItem> items = ship.getCargo().inventory().stream()
				.sorted((c1, c2) -> Integer.compare(c2.units(), c1.units()))
				.toList();

		for (final CargoItem item : items) {
			final Product product = new Product(item.symbol());
			final Optional<Deque<Waypoint>> optionalMarketPath = marketplaceManager.getClosestTradingWaypointPath(ship, product);
			if (optionalMarketPath.isEmpty()) {
				// Nothing buys this product within the ship's range, skip it
				continue;
			}
			final Deque<Waypoint> buyingMarketPath = optionalMarketPath.get();

			// Save the selling market so that we know when we're there
			job.setSellingPoint(buyingMarketPath.peekLast());

			// Start traveling along the path to the market
			final Waypoint nextWaypoint = buyingMarketPath.remove();
			final Instant arrival = navHelper.navigate(ship, nextWaypoint);

			final String message = String.format(
					"Identified market to sell extracted goods, ship %s will arrive at %s at %s",
					ship.getSymbol(), nextWaypoint.getSymbol(), arrival);
			websocketReporter.fireShipEvent(ship.getSymbol(), message);
			log.infof(message);

			job.setSellingPath(buyingMarketPath);
			return arrival;
		}
		//TODO: Go to more efficient mode and travel to one of the markets this way
		throw new IllegalStateException("No waypoints within range which purchase current cargo");
	}

	private void sellItems(final MiningShipJob job) {
		final Ship ship = job.getShip();

		marketplaceRequester.dockAndSellItems(ship, job.getSellingPoint(), ship.getCargo().inventory());
	}

}
