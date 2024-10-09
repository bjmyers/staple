package org.psu.trademanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.trademanager.dto.TradeRoute;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Builds and stores potential {@link TradeRoute}s for a given system
 */
@ApplicationScoped
public class RouteManager {

	private MarketplaceManager marketplaceManager;

	private List<TradeRoute> tradeRoutes;

	@Inject
	public RouteManager(final MarketplaceManager marketplaceManager) {
		this.marketplaceManager = marketplaceManager;
		this.tradeRoutes = null;
	}

	void buildTradeRoutes() {

		final Map<Waypoint, MarketInfo> systemMarketInfo = marketplaceManager.getAllMarketInfo();

		final List<TradeRoute> routes = new ArrayList<>();

		for (Entry<Waypoint, MarketInfo> exportingWaypoint : systemMarketInfo.entrySet()) {

			for (Entry<Waypoint, MarketInfo> importingWaypoint : systemMarketInfo.entrySet()) {

				if (exportingWaypoint.getKey().equals(importingWaypoint.getKey())) {
					// The same waypoint will not export to itself
					continue;
				}

				final List<Product> exports = exportingWaypoint.getValue()
						.getPotentialExports(importingWaypoint.getValue());
				if (!exports.isEmpty()) {
					routes.add(new TradeRoute(exportingWaypoint.getKey(), importingWaypoint.getKey(), exports));
				}
			}
		}

		this.tradeRoutes = routes;
	}

	List<TradeRoute> getTradeRoutes() {
		return this.tradeRoutes;
	}

	/**
	 * @param ship The {@link Ship} which is to perform a trade route
	 * @return The route with the shorted travel distance for the ship, or an empty
	 *         optional if no route is possible
	 */
	public Optional<TradeRoute> getClosestRoute(final Ship ship) {
		if (this.tradeRoutes == null) {
			// Lazy load trade routes
			buildTradeRoutes();
		}

		// The route whose total travel distance for the ship is smallest
		// Filter out impossible routes
		return this.tradeRoutes.stream().filter(t -> t.isPossible(ship))
				.min((tr1, tr2) -> Double.compare(ship.distTo(tr1.getExportWaypoint()) + tr1.getDistance(),
						ship.distTo(tr2.getExportWaypoint()) + tr2.getDistance()));
	}

}
