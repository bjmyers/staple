package org.psu.trademanager;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

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
import org.psu.trademanager.dto.TradeRoute;

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

	private RequestThrottler throttler;
	private NavigationClient navigationClient;
	private AccountManager accountManager;
	private MarketplaceRequester marketplaceRequester;
	private MarketplaceManager marketplaceManager;
	private RouteManager routeManager;

	@Inject
	public TradeShipManager(final RequestThrottler throttler, @RestClient final NavigationClient navigationClient,
			final AccountManager accountManager, final MarketplaceRequester marketplaceRequester,
			final MarketplaceManager marketplaceManager, final RouteManager routeManager) {
		this.throttler = throttler;
		this.navigationClient = navigationClient;
		this.accountManager = accountManager;
		this.marketplaceRequester = marketplaceRequester;
		this.marketplaceManager = marketplaceManager;
		this.routeManager = routeManager;
	}

	/**
	 * Sends a single ship on trade routes until terminated. This function will not return
	 * TODO: Hook this up to a job queue so that we can handle more than a single trade ship
	 * @param tradeShip The trade ship to manage
	 */
	public void manageTradeShip(final Ship tradeShip) {
		final String shipId = tradeShip.getSymbol();

		while (true) {
			final TradeRoute closestRoute = routeManager.getClosestRoute(tradeShip).get();
			log.infof("Using Trade Route with Starting point %s and ending point %s",
					closestRoute.getExportWaypoint().getSymbol(), closestRoute.getImportWaypoint().getSymbol());

			final String firstWaypointSymbol = closestRoute.getExportWaypoint().getSymbol();
			// If we're currently at the first waypoint, there's no need to navigate to it
			if (!tradeShip.getNav().getWaypointSymbol().equals(firstWaypointSymbol)) {
				final ShipNavigation newNav = orbitAndNavigate(shipId, firstWaypointSymbol);
				tradeShip.setNav(newNav);
			}

			// Dock and purchase goods
			throttler.throttle(() -> navigationClient.dock(shipId));

			// TODO: Remove this when space traders fixes their bug, it takes a while for markets to realize
			// a ship is docked at them
			try {
				Thread.sleep(Duration.ofSeconds(5));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Force an update to we know the most up to date prices and trade limits
			final MarketInfo exportMarketInfo = marketplaceManager.updateMarketInfo(closestRoute.getExportWaypoint());
			final int totalCredits = accountManager.getCredits();
			final List<TradeRequest> purchaseRequests = exportMarketInfo.buildPurchaseRequest(closestRoute.getGoods(),
					tradeShip.getRemainingCargo(), totalCredits);

			int total = 0;
			for (final TradeRequest tradeRequest : purchaseRequests) {
				final TradeResponse purchaseResponse = throttler
						.throttle(() -> marketplaceRequester.purchase(shipId, tradeRequest));

				total += purchaseResponse.getTransaction().getTotalPrice();
				log.infof("Purchased %s unit(s) of %s for %s credits", tradeRequest.getUnits(),
						tradeRequest.getSymbol(), purchaseResponse.getTransaction().getTotalPrice());
			}
			log.infof("Total Purchase Price: %s", total);

			final ShipNavigation newNav = orbitAndNavigate(shipId, closestRoute.getImportWaypoint().getSymbol());
			tradeShip.setNav(newNav);

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
			final MarketInfo importMarketInfo = marketplaceManager.updateMarketInfo(closestRoute.getImportWaypoint());

			// Re-balance the sell requests because trade limits might be different
			final List<TradeRequest> sellRequests = importMarketInfo.rebalanceTradeRequests(purchaseRequests);

			int sellTotal = 0;
			for (final TradeRequest tradeRequest : sellRequests) {
				final TradeResponse purchaseResponse = throttler
						.throttle(() -> marketplaceRequester.sell(shipId, tradeRequest));

				sellTotal += purchaseResponse.getTransaction().getTotalPrice();
				log.infof("Sold %s unit(s) of %s for %s credits", tradeRequest.getUnits(),
						tradeRequest.getSymbol(), purchaseResponse.getTransaction().getTotalPrice());
			}
			log.infof("Total Sell Price: %s", sellTotal);

			if (marketplaceManager.getMarketInfo(closestRoute.getImportWaypoint()).sellsProduct(Product.FUEL)) {
				throttler.throttle(() -> marketplaceRequester.refuel(shipId));
				log.infof("Refuled ship %s", shipId);
			}
			else {
				log.warnf("Unable to refuel ship %s at waypoint %s", shipId,
						closestRoute.getImportWaypoint().getSymbol());
			}

			return;
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
		try {
			Thread.sleep(tripTime);
		} catch (InterruptedException e) {
			log.error("Interrupted while waiting for navigation");
			log.error(e);
		}

		return navResponse.getData().getNav();
	}

}
