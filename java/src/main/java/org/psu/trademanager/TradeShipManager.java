package org.psu.trademanager;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.psu.spacetraders.api.AccountManager;
import org.psu.spacetraders.api.MarketplaceRequester;
import org.psu.spacetraders.api.NavigationClient;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.NavigationRequest;
import org.psu.spacetraders.dto.NavigationResponse;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipNavigation;
import org.psu.spacetraders.dto.TradeRequest;
import org.psu.spacetraders.dto.TradeResponse;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.trademanager.dto.TradeRoute;
import org.psu.trademanager.dto.TradeShipJob;
import org.psu.trademanager.dto.TradeShipJob.State;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Manages Trade Ships, including generating routes for them and ensuring that
 * they perform the routes accurately
 */
@JBossLog
@ApplicationScoped
public class TradeShipManager {

	private Duration navigationPad;
	private RequestThrottler throttler;
	private NavigationClient navigationClient;
	private AccountManager accountManager;
	private MarketplaceRequester marketplaceRequester;
	private MarketplaceManager marketplaceManager;
	private RouteManager routeManager;

	@Inject
	public TradeShipManager(@ConfigProperty(name = "app.navigation-pad-ms") final int navigationPad,
			final RequestThrottler throttler, @RestClient final NavigationClient navigationClient,
			final AccountManager accountManager, final MarketplaceRequester marketplaceRequester,
			final MarketplaceManager marketplaceManager, final RouteManager routeManager) {
		this.navigationPad = Duration.ofMillis(navigationPad);
		this.throttler = throttler;
		this.navigationClient = navigationClient;
		this.accountManager = accountManager;
		this.marketplaceRequester = marketplaceRequester;
		this.marketplaceManager = marketplaceManager;
		this.routeManager = routeManager;
	}

	public TradeShipJob createJob(final Ship ship) {
		final TradeRoute closestRoute = routeManager.getClosestRoute(ship).get();
		return new TradeShipJob(ship, closestRoute);
	}

	/**
	 * Performs the next step in a {@link TradeShipJob}
	 * @param job the {@link TradeShipJob}
	 * @return The updated {@link TradeShipJob}, with an updated nextAction time
	 */
	public TradeShipJob manageTradeShip(final TradeShipJob job) {

		// The state of the job indicates what is was doing before it reached the manager
		switch (job.getState()) {
		case NOT_STARTED:
			final Instant exportArrival = navigate(job.getShip(), job.getRoute().getExportWaypoint());
			job.setNextAction(exportArrival);
			job.setState(State.TRAVELING_TO_EXPORT);
			break;
		case TRAVELING_TO_EXPORT:
			final List<TradeRequest> purchases = purchaseGoods(job);
			job.setPurchases(purchases);
			final Instant importArrival = navigate(job.getShip(), job.getRoute().getImportWaypoint());
			job.setNextAction(importArrival);
			job.setState(State.TRAVELING_TO_IMPORT);
			break;
		case TRAVELING_TO_IMPORT:
			sellGoods(job);
			// Make a whole new job and return it
			return createJob(job.getShip());
		}

		return job;
	}

	private Instant navigate(final Ship ship, final Waypoint waypoint) {

		final String waypointSymbol = waypoint.getSymbol();
		// If we're currently at the first waypoint, there's no need to navigate to it
		if (!ship.getNav().getWaypointSymbol().equals(waypointSymbol)) {
			final ShipNavigation newNav = orbitAndNavigate(ship.getSymbol(), waypointSymbol);
			ship.setNav(newNav);
			return newNav.getRoute().getArrival().plus(navigationPad);
		}
		// Navigation is done!
		return Instant.now();
	}

	private List<TradeRequest> purchaseGoods(final TradeShipJob job) {
		final Ship ship = job.getShip();
		final TradeRoute route = job.getRoute();

		// Dock and purchase goods
		throttler.throttle(() -> navigationClient.dock(ship.getSymbol()));

		// TODO: Remove this when space traders fixes their bug, it takes a while for markets to realize
		// a ship is docked at them
		try {
			Thread.sleep(Duration.ofSeconds(5));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Force an update to we know the most up to date prices and trade limits
		final MarketInfo exportMarketInfo = marketplaceManager.updateMarketInfo(route.getExportWaypoint());
		final int totalCredits = accountManager.getCredits();
		final List<TradeRequest> purchaseRequests = exportMarketInfo.buildPurchaseRequest(route.getGoods(),
				ship.getRemainingCargo(), totalCredits);

		int total = 0;
		for (final TradeRequest tradeRequest : purchaseRequests) {
			final TradeResponse purchaseResponse = throttler
					.throttle(() -> marketplaceRequester.purchase(ship.getSymbol(), tradeRequest));

			total += purchaseResponse.getTransaction().getTotalPrice();
			log.infof("Purchased %s unit(s) of %s for %s credits", tradeRequest.getUnits(),
					tradeRequest.getSymbol(), purchaseResponse.getTransaction().getTotalPrice());
		}
		log.infof("Total Purchase Price: %s", total);

		return purchaseRequests;
	}

	private void sellGoods(final TradeShipJob job) {
		final String shipId = job.getShip().getSymbol();
		final TradeRoute route = job.getRoute();

		// Dock and sell goods
		throttler.throttle(() -> navigationClient.dock(shipId));

		// TODO: Remove this when space traders fixes their bug, it takes a while for markets to realize
		// a ship is docked at them
		try {
			Thread.sleep(Duration.ofSeconds(5));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Force an update to we know the most up to date prices and trade limits
		final MarketInfo importMarketInfo = marketplaceManager.updateMarketInfo(job.getRoute().getImportWaypoint());

		// Re-balance the sell requests because trade limits might be different
		final List<TradeRequest> sellRequests = importMarketInfo.rebalanceTradeRequests(job.getPurchases());

		int sellTotal = 0;
		for (final TradeRequest tradeRequest : sellRequests) {
			final TradeResponse purchaseResponse = throttler
					.throttle(() -> marketplaceRequester.sell(shipId, tradeRequest));

			sellTotal += purchaseResponse.getTransaction().getTotalPrice();
			log.infof("Sold %s unit(s) of %s for %s credits", tradeRequest.getUnits(),
					tradeRequest.getSymbol(), purchaseResponse.getTransaction().getTotalPrice());
		}
		log.infof("Total Sell Price: %s", sellTotal);

		if (marketplaceManager.getMarketInfo(route.getImportWaypoint()).sellsProduct(Product.FUEL)) {
			throttler.throttle(() -> marketplaceRequester.refuel(shipId));
			log.infof("Refuled ship %s", shipId);
		}
		else {
			log.warnf("Unable to refuel ship %s at waypoint %s", shipId,
					route.getImportWaypoint().getSymbol());
		}
	}

	private ShipNavigation orbitAndNavigate(final String shipId, final String waypointSymbol) {
		throttler.throttle(() -> navigationClient.orbit(shipId));

		log.infof("Navigating to %s", waypointSymbol);
		final NavigationRequest navRequest = new NavigationRequest(waypointSymbol);
		final DataWrapper<NavigationResponse> navResponse = throttler
				.throttle(() -> navigationClient.navigate(shipId, navRequest));

		final Instant arrivalTime = navResponse.getData().getNav().getRoute().getArrival();
		final Duration tripTime = Duration.between(Instant.now(), arrivalTime);
		log.infof("Navigation will take %s", tripTime);

		return navResponse.getData().getNav();
	}

}
