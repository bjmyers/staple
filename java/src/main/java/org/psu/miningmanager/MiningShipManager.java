package org.psu.miningmanager;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

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

	@Inject
	public MiningShipManager(@ConfigProperty(name = "app.cooldown-pad-ms") final int cooldownPad,
			@RestClient final SurveyClient surveyClient, final RequestThrottler throttler,
			final MiningSiteManager siteManager, final NavigationHelper navHelper,
			final MarketplaceManager marketplaceManager, final MarketplaceRequester marketplaceRequester) {
		this.cooldownPad = Duration.ofMillis(cooldownPad);
		this.surveyClient = surveyClient;
		this.throttler = throttler;
		this.siteManager = siteManager;
		this.navHelper = navHelper;
		this.marketplaceManager = marketplaceManager;
		this.marketplaceRequester = marketplaceRequester;
	}

	public MiningShipJob createJob(final Ship ship) {

		final Waypoint destination = siteManager.getMiningSite(ship.getNav().getWaypointSymbol());
		if (destination != null) {
			// The ship is at or traveling to a mining site
			if (ship.getNav().getRoute().getArrival().compareTo(Instant.now()) > 0) {
				// The ship will arrive in the future
				final MiningShipJob job = new MiningShipJob(ship, destination);
				job.setState(State.TRAVELING_TO_RESOURCE);
				job.setNextAction(ship.getNav().getRoute().getArrival().plus(cooldownPad));
				return job;
			}
			// The ship is already at a mining site
			if (ship.getRemainingCargo() > 0) {
				// We're not full, start by surveying and then start extracting
				final MiningShipJob job = new MiningShipJob(ship, destination);
				job.setState(State.TRAVELING_TO_RESOURCE);
				return job;
			}
			// We're full, let the job calculate the marketplace to sell
			final MiningShipJob job = new MiningShipJob(ship, destination);
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
				// The destination does indeed buy something that we have
				// Extraction point doesn't matter, we've already extracted everything
				final MiningShipJob job = new MiningShipJob(ship, null);
				job.setState(State.TRAVELING_TO_MARKET);
				job.setNextAction(ship.getNav().getRoute().getArrival());
				job.setSellingWaypoint(destinationMarketInfo.getKey());
				return job;
			}
		}

		// We're not in the middle of a mining job, make a new one
		final Waypoint extractionSite = siteManager.getClosestMiningSite(ship).get();
		return new MiningShipJob(ship, extractionSite);
	}

	public MiningShipJob manageMiningShip(final MiningShipJob job) {
		final Ship ship = job.getShip();
		final String shipId = ship.getSymbol();

		// The state of the job indicates what is was doing before it reached the manager
		switch (job.getState()) {
		case NOT_STARTED:
			log.infof("Ship %s traveling to extraction waypoint %s", shipId,
					job.getExtractionPoint().getSymbol());
			final Instant extractionArrival = navHelper.navigate(ship, job.getExtractionPoint());
			job.setNextAction(extractionArrival);
			job.setState(State.TRAVELING_TO_RESOURCE);
			break;
		case TRAVELING_TO_RESOURCE:
			final SurveyResponse surveyResponse = throttler.throttle(() -> surveyClient.survey(shipId)).getData();
			job.setSurveys(surveyResponse.getSurveys());
			final Instant surveyCooldownComplete = Instant.now()
					.plus(Duration.ofSeconds(surveyResponse.getCooldown().getTotalSeconds()));
			job.setNextAction(surveyCooldownComplete);
			job.setState(State.SURVEYING);
			log.infof("Finished Surveying, found %s sites, ship %s in cooldown until %s", job.getSurveys().size(),
					shipId, job.getNextAction());
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
			log.infof("Ship %s extracting resources", shipId);
			final ExtractResponse extractResponse = throttler.throttle(() -> surveyClient.extractSurvey(shipId, survey))
					.getData();
			ship.setCargo(extractResponse.getCargo());

			final Instant extractCooldownComplete = Instant.now()
					.plus(Duration.ofSeconds(extractResponse.getCooldown().getTotalSeconds()));
			job.setNextAction(extractCooldownComplete);
			job.setState(State.EXTRACTING);
			log.infof("Ship %s finished extraction, ready to extract again at %s",
					shipId, job.getNextAction());
			break;
		case TRAVELING_TO_MARKET:
			sellItems(job);
			// Make a whole new job and return it
			return createJob(job.getShip());
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
			final Optional<Waypoint> optionalMarket = marketplaceManager.getClosestTradingWaypoint(ship, product);
			if (optionalMarket.isEmpty()) {
				// Nothing buys this product, skip it
				continue;
			}
			final Waypoint buyingMarket = optionalMarket.get();
			if (ship.canTravelTo(buyingMarket)) {
				final Instant arrival = navHelper.navigate(ship, buyingMarket);
				log.infof("Identified market %s to sell extracted goods, ship %s will arrive at %s",
						buyingMarket.getSymbol(), ship.getSymbol(), arrival);
				job.setSellingWaypoint(buyingMarket);
				return arrival;
			}
		}
		//TODO: Go to more efficient mode and travel to one of the markets this way
		throw new IllegalStateException("No waypoints within range which purchase current cargo");
	}

	private void sellItems(final MiningShipJob job) {
		final Ship ship = job.getShip();

		marketplaceRequester.dockAndSellItems(ship, job.getSellingWaypoint(), ship.getCargo().inventory());
	}

}
