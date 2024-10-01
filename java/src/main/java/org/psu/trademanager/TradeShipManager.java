package org.psu.trademanager;

import java.util.List;
import java.util.Map;

import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Ship;
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

	private RouteBuilder routeBuilder;

	@Inject
	public TradeShipManager(final RouteBuilder routeBuilder) {
		this.routeBuilder = routeBuilder;
	}

	/**
	 * Sends a single ship on trade routes until terminated. This function will not return
	 * TODO: Hook this up to a job queue so that we can handle more than a single trade ship
	 * @param systemMarketInfo All of the market information for a system
	 * @param tradeShip The trade ship to manage
	 */
	public void manageTradeShip(final Map<Waypoint, MarketInfo> systemMarketInfo, final Ship tradeShip) {

		log.info("Building trade routes");
		final List<TradeRoute> routes = routeBuilder.buildTradeRoutes(systemMarketInfo);
		log.infof("Build %s trade routes", routes.size());

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
			return;
		}

	}

}
