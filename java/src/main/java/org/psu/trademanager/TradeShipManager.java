package org.psu.trademanager;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.psu.spacetraders.api.MarketplaceClient;
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
	private RouteBuilder routeBuilder;
	private MarketplaceClient marketClient;
	private NavigationClient navigationClient;

	@Inject
	public TradeShipManager(final RequestThrottler throttler, final RouteBuilder routeBuilder,
			@RestClient final MarketplaceClient marketClient, @RestClient final NavigationClient navigationClient) {
		this.throttler = throttler;
		this.routeBuilder = routeBuilder;
		this.marketClient = marketClient;
		this.navigationClient = navigationClient;
	}

	/**
	 * Sends a single ship on trade routes until terminated. This function will not return
	 * TODO: Hook this up to a job queue so that we can handle more than a single trade ship
	 * @param systemMarketInfo All of the market information for a system
	 * @param tradeShip The trade ship to manage
	 */
	public void manageTradeShip(final Map<Waypoint, MarketInfo> systemMarketInfo, final Ship tradeShip) {
		final String shipId = tradeShip.getSymbol();

		log.info("Building trade routes");
		final List<TradeRoute> routes = routeBuilder.buildTradeRoutes(systemMarketInfo);
		log.infof("Built %s trade routes", routes.size());

		if (routes.isEmpty()) {
			log.warn("Unable to find any valid trade routes for this system, terminating trade manager");
			return;
		}

		while (true) {
			// The route whose start point is closest to the ship's current position
			final TradeRoute closestRoute = routes.stream().min((way1, way2) -> Double
					.compare(tradeShip.distTo(way1.getExportWaypoint()), tradeShip.distTo(way2.getExportWaypoint())))
					.get();
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
			// Trade routes always need at least one Product to trade
			final Product productToPurchase = closestRoute.getGoods().stream().findFirst().get();
			// TODO: Make this code purchase more than just one unit
			final int quantityToTrade = 1;
			final TradeRequest purchaseRequest = new TradeRequest(productToPurchase.getSymbol(), quantityToTrade);
			final DataWrapper<TradeResponse> purchaseResponse = throttler
					.throttle(() -> marketClient.purchase(shipId, purchaseRequest));

			log.infof("Purchased one unit of %s for %s credits", productToPurchase.getSymbol(),
					purchaseResponse.getData().getTransaction().getTotalPrice());

			final ShipNavigation newNav = orbitAndNavigate(shipId, closestRoute.getImportWaypoint().getSymbol());
			tradeShip.setNav(newNav);

			// Dock and sell goods
			throttler.throttle(() -> navigationClient.dock(shipId));
			// Trade routes always need at least one Product to trade
			final Product productToSell = closestRoute.getGoods().stream().findFirst().get();
			final TradeRequest sellRequest = new TradeRequest(productToSell.getSymbol(), quantityToTrade);
			final DataWrapper<TradeResponse> sellResponse = throttler
					.throttle(() -> marketClient.sell(shipId, sellRequest));

			log.infof("Sold one unit of %s for %s credits", productToSell.getSymbol(),
					sellResponse.getData().getTransaction().getTotalPrice());

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
