package org.psu.trademanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.trademanager.dto.TradeRoute;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Builds potential {@link TradeRoute}s given the market information of a system
 */
@ApplicationScoped
public class RouteBuilder {

	public List<TradeRoute> buildTradeRoutes(final Map<Waypoint, MarketInfo> systemMarketInfo) {

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

		return routes;
	}

}
