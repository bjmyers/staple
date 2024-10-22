package org.psu.trademanager;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.psu.init.RandomProvider;
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
	private RandomProvider randomProvider;

	private List<TradeRoute> tradeRoutes;

	@Inject
	public RouteManager(final MarketplaceManager marketplaceManager, final RandomProvider randomProvider) {
		this.marketplaceManager = marketplaceManager;
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

	public TradeRoute getBestRoute(final Ship ship) {
		if (this.tradeRoutes == null) {
			// Lazy load trade routes
			buildTradeRoutes();
		}

		// Find all possible routes
		final List<TradeRoute> possibleRoutes = this.tradeRoutes.stream().filter(t -> t.isPossible(ship)).toList();

		if (possibleRoutes.isEmpty()) {
			return null;
		}

		// Includes only known routes
		final Map<TradeRoute, RouteProfit> profitByRoute = possibleRoutes.stream()
				.map(r -> new AbstractMap.SimpleEntry<TradeRoute, RouteProfit>(r, getPotentialProfit(r)))
				.filter(entry -> entry.getValue() != null).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		final Optional<TradeRoute> shortestUnknownRoute = possibleRoutes.stream()
				.filter(r -> !profitByRoute.containsKey(r))
				.min(Comparator.comparing(r -> ship.distTo(r.getExportWaypoint()) + r.getDistance()));

		final Optional<TradeRoute> mostProfitableRoute = profitByRoute.entrySet().stream()
				.filter(e -> e.getValue() != null).max(Comparator.comparing(e -> e.getValue().profit()))
				.map(Entry::getKey);

		if (mostProfitableRoute.isEmpty()) {
			// All routes are unknown, go with shortest route
			log.info("Found no routes with known profits, picking shortest route");
			return shortestUnknownRoute.get();
		}
		if (shortestUnknownRoute.isEmpty()) {
			// All routes are known, go with the most profitable route
			final TradeRoute chosenRoute = mostProfitableRoute.get();
			final RouteProfit routeProfit = profitByRoute.get(chosenRoute);
			log.infof("All routes have known profits, picking the route with potential profit of %s per %s",
					routeProfit.profit(), routeProfit.itemToSell());
			chosenRoute.setGoods(List.of(routeProfit.itemToSell()));
			return chosenRoute;
		}
		// For now, randomly pick the shortest or most profitable, balancing exploring and making money
		if (randomProvider.nextDouble() < 0.5) {
			final TradeRoute chosenRoute = mostProfitableRoute.get();
			final RouteProfit routeProfit = profitByRoute.get(chosenRoute);
			log.infof("Picking most profitable route, with potential profit of %s per %s",
					routeProfit.profit(), routeProfit.itemToSell());
			chosenRoute.setGoods(List.of(routeProfit.itemToSell()));
			return chosenRoute;
		}
		log.info("Picking shortest route");
		return shortestUnknownRoute.get();
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

	private record RouteProfit(Integer profit, Product itemToSell) {};

}
