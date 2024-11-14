package org.psu.trademanager;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

import org.psu.init.RandomProvider;
import org.psu.navigation.NavigationPath;
import org.psu.navigation.RefuelPathCalculator;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.TradeGood;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.trademanager.dto.TradeRoute;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Builds and stores potential {@link TradeRoute}s for a given system
 */
@JBossLog
@ApplicationScoped
public class RouteManager {

	private MarketplaceManager marketplaceManager;
	private RefuelPathCalculator refuelPathCalculator;
	private RandomProvider randomProvider;

	private List<TradeRoute> tradeRoutes;

	@Inject
	public RouteManager(final MarketplaceManager marketplaceManager, final RefuelPathCalculator refuelPathCalculator,
			final RandomProvider randomProvider) {
		this.marketplaceManager = marketplaceManager;
		this.refuelPathCalculator = refuelPathCalculator;
		this.randomProvider = randomProvider;
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

	public RouteResponse getBestRoute(final Ship ship) {
		if (this.tradeRoutes == null) {
			// Lazy load trade routes
			buildTradeRoutes();
		}

		// Find all possible routes

		final Map<TradeRoute, NavigationPath> possibleRoutes = this.tradeRoutes.stream()
				.map(r -> new AbstractMap.SimpleEntry<TradeRoute, NavigationPath>(r, getTotalPath(r, ship)))
				.filter(e -> e.getValue() != null).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		log.infof("Found %s possible routes", possibleRoutes.size());

		if (possibleRoutes.isEmpty()) {
			return null;
		}

		// Includes only known routes
		final Map<TradeRoute, RouteProfit> profitByRoute = possibleRoutes.keySet().stream()
				.map(r -> new AbstractMap.SimpleEntry<TradeRoute, RouteProfit>(r, getPotentialProfit(r)))
				.filter(entry -> entry.getValue() != null).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		final Optional<Entry<TradeRoute, NavigationPath>> shortestUnknownRoute = possibleRoutes.entrySet().stream()
				.filter(entry -> !profitByRoute.containsKey(entry.getKey()))
				.min(Comparator.comparing(entry -> entry.getValue().getLength()));

		final Optional<Entry<TradeRoute, RouteProfit>> mostProfitableRoute = profitByRoute.entrySet().stream()
				.filter(e -> e.getValue() != null).max(Comparator.comparing(e -> e.getValue().profit()));

		if (shortestUnknownRoute.isEmpty()) {
			// All routes are known, go with the most profitable route
			final Entry<TradeRoute, RouteProfit> chosenRouteEntry = mostProfitableRoute.get();
			final TradeRoute chosenRoute = chosenRouteEntry.getKey();
			final RouteProfit routeProfit = chosenRouteEntry.getValue();
			log.infof("All routes have known profits, picking the route with potential profit of %s per %s",
					routeProfit.profit(), routeProfit.itemToSell());
			chosenRoute.setGoods(List.of(routeProfit.itemToSell()));
			chosenRoute.setKnown(true);
			final NavigationPath path = possibleRoutes.get(chosenRoute);
			return new RouteResponse(chosenRoute, path.getWaypoints());
		}
		if (mostProfitableRoute.isEmpty() || mostProfitableRoute.get().getValue().profit() < 0) {
			// All routes are unknown or the most profitable route is not profitable, go
			// with shortest route
			log.info("Found no routes with known profits, picking shortest route");
			final TradeRoute shortestRoute = shortestUnknownRoute.get().getKey();
			shortestRoute.setKnown(false);
			final NavigationPath path = shortestUnknownRoute.get().getValue();
			return new RouteResponse(shortestRoute, path.getWaypoints());
		}
		// For now, randomly pick the shortest or most profitable, balancing exploring and making money
		if (randomProvider.nextDouble() < 0.5) {
			final Entry<TradeRoute, RouteProfit> chosenRouteEntry = mostProfitableRoute.get();
			final TradeRoute chosenRoute = chosenRouteEntry.getKey();
			final RouteProfit routeProfit = chosenRouteEntry.getValue();
			log.infof("Picking most profitable route, with potential profit of %s per %s",
					routeProfit.profit(), routeProfit.itemToSell());
			chosenRoute.setGoods(List.of(routeProfit.itemToSell()));
			chosenRoute.setKnown(true);
			final NavigationPath path = possibleRoutes.get(chosenRoute);
			return new RouteResponse(chosenRoute, path.getWaypoints());
		}
		log.info("Picking shortest route");
		final TradeRoute shortestRoute = shortestUnknownRoute.get().getKey();
		shortestRoute.setKnown(false);
		final NavigationPath path = shortestUnknownRoute.get().getValue();
		return new RouteResponse(shortestRoute, path.getWaypoints());
	}

	private RouteProfit getPotentialProfit(final TradeRoute route) {
		final MarketInfo exportingMarket = marketplaceManager.getMarketInfo(route.getExportWaypoint());
		final MarketInfo importingMarket = marketplaceManager.getMarketInfo(route.getImportWaypoint());

		if (exportingMarket.getTradeGoods() == null || importingMarket.getTradeGoods() == null) {
			// We don't know price information for one of the two markets, can't calculate profit
			return null;
		}

		// This variable will not remain null, because we know there is at least one potential export
		// Otherwise, this trade route would not have been originally created
		Product mostProfitableProduct = null;
		int maxProfit = Integer.MIN_VALUE;
		for (final Product product : exportingMarket.getPotentialExports(importingMarket)) {
			// We know both of these will be present because the route's goods were set based on what the
			// two waypoints sell
			// Assumes that markets don't change which goods they buy/sell, and I'm pretty sure they can't
			final TradeGood exportTradeGood = exportingMarket.getTradeGoods().stream()
					.filter(t -> t.getSymbol().equals(product.getSymbol())).findFirst().get();
			final TradeGood importTradeGood = importingMarket.getTradeGoods().stream()
					.filter(t -> t.getSymbol().equals(product.getSymbol())).findFirst().get();

			final int productProfit = importTradeGood.getSellPrice() - exportTradeGood.getPurchasePrice();
			if (productProfit > maxProfit) {
				mostProfitableProduct = product;
				maxProfit = productProfit;
			}
		}

		return new RouteProfit(maxProfit, mostProfitableProduct);
	}

	private NavigationPath getTotalPath(final TradeRoute route, final Ship ship) {
		final NavigationPath pathToExport = refuelPathCalculator.determineShortestRoute(ship,
				route.getExportWaypoint());
		final NavigationPath routePath = refuelPathCalculator.determineShortestRoute(route.getExportWaypoint(),
				// Use capacity as current fuel because we assume it refuels at the export
				route.getImportWaypoint(), ship.getFuel().capacity(), ship.getFuel().capacity());

		if (pathToExport == null || routePath == null) {
			return null;
		}
		return NavigationPath.combine(pathToExport, routePath);
	}

	public record RouteResponse(TradeRoute route, Queue<Waypoint> waypoints) {};

	private record RouteProfit(Integer profit, Product itemToSell) {};

}
