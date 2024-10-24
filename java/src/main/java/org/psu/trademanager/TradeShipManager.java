package org.psu.trademanager;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.psu.spacetraders.api.AccountManager;
import org.psu.spacetraders.api.MarketplaceRequester;
import org.psu.spacetraders.api.NavigationHelper;
import org.psu.spacetraders.dto.CargoItem;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Ship;
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
	private NavigationHelper navigationHelper;
	private AccountManager accountManager;
	private MarketplaceRequester marketplaceRequester;
	private MarketplaceManager marketplaceManager;
	private RouteManager routeManager;

	@Inject
	public TradeShipManager(@ConfigProperty(name = "app.navigation-pad-ms") final int navigationPad,
			final NavigationHelper navigationHelper,
			final AccountManager accountManager, final MarketplaceRequester marketplaceRequester,
			final MarketplaceManager marketplaceManager, final RouteManager routeManager) {
		this.navigationPad = Duration.ofMillis(navigationPad);
		this.navigationHelper = navigationHelper;
		this.accountManager = accountManager;
		this.marketplaceRequester = marketplaceRequester;
		this.marketplaceManager = marketplaceManager;
		this.routeManager = routeManager;
	}

	public TradeShipJob createJob(final Ship ship) {

		// If the ship has goods in its cargo hold, see if we can sell them
		if (ship.getCargo().units() > 0) {

			// Sorted so that the item with the highest count is first
			final List<CargoItem> items = ship.getCargo().inventory().stream()
					.sorted((c1, c2) -> Integer.compare(c2.units(), c1.units()))
					.toList();

			// First check the destination to see if it buys anything we've got
			final Optional<Entry<Waypoint, MarketInfo>> optionalDestinationMarketInfo = marketplaceManager
					.getMarketInfoById(ship.getNav().getRoute().getDestination().getSymbol());
			if (optionalDestinationMarketInfo.isPresent()) {
				final Entry<Waypoint, MarketInfo> destinationMarketInfo = optionalDestinationMarketInfo.get();
				final List<String> importProductSymbols = destinationMarketInfo.getValue().getImports().stream()
						.map(Product::getSymbol).toList();
				final List<CargoItem> itemsToSell = items.stream()
						.filter(i -> importProductSymbols.contains(i.symbol())).toList();
				if (itemsToSell.size() > 0) {
					// We are able to sell some of our inventory at the destination, build a trade
					// route
					final List<Product> productsToSell = itemsToSell.stream().map(c -> new Product(c.symbol()))
							.toList();
					final TradeRoute route = new TradeRoute(null, destinationMarketInfo.getKey(), productsToSell);
					final TradeShipJob job = new TradeShipJob(ship, route);
					job.setState(State.TRAVELING_TO_IMPORT);
					job.setNextAction(ship.getNav().getRoute().getArrival().plus(navigationPad));
					return job;
				}
			}
		}

		// Nothing in the cargo bay, let's go and make normal trade routes
		final TradeRoute bestRoute = routeManager.getBestRoute(ship);
		return new TradeShipJob(ship, bestRoute);
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
			log.infof("Ship %s traveling to export waypoint %s", job.getShip().getSymbol(),
					job.getRoute().getExportWaypoint().getSymbol());
			final Instant exportArrival = navigationHelper.navigate(job.getShip(), job.getRoute().getExportWaypoint());
			job.setNextAction(exportArrival);
			job.setState(State.TRAVELING_TO_EXPORT);
			break;
		case TRAVELING_TO_EXPORT:
			purchaseGoods(job);
			log.infof("Ship %s traveling to import waypoint %s", job.getShip().getSymbol(),
					job.getRoute().getImportWaypoint().getSymbol());
			final Instant importArrival = navigationHelper.navigate(job.getShip(), job.getRoute().getImportWaypoint());
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

	private List<TradeRequest> purchaseGoods(final TradeShipJob job) {
		final Ship ship = job.getShip();
		final TradeRoute route = job.getRoute();

		// Dock and purchase goods
		navigationHelper.dock(ship);

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
				ship.getRemainingCargo(), totalCredits, route.isKnown());

		int total = 0;
		for (final TradeRequest tradeRequest : purchaseRequests) {
			final TradeResponse purchaseResponse = marketplaceRequester.purchase(ship, tradeRequest);

			total += purchaseResponse.getTransaction().getTotalPrice();
			log.infof("Purchased %s unit(s) of %s for %s credits", tradeRequest.getUnits(),
					tradeRequest.getSymbol(), purchaseResponse.getTransaction().getTotalPrice());
		}
		log.infof("Total Purchase Price: %s", total);
		route.setPurchasePrice(total);

		return purchaseRequests;
	}

	private void sellGoods(final TradeShipJob job) {
		final Ship ship = job.getShip();
		final TradeRoute route = job.getRoute();

		final List<String> productsToSell = route.getGoods().stream().map(Product::getSymbol).toList();
		final List<CargoItem> cargoToSell = ship.getCargo().inventory().stream()
				.filter(c -> productsToSell.contains(c.symbol())).toList();

		final Integer sellPrice = marketplaceRequester.dockAndSellItems(ship, route.getImportWaypoint(), cargoToSell);
		if (route.getPurchasePrice() != null) {
			log.infof("Trade route for ship %s made a total profit of %s", ship.getSymbol(),
					sellPrice - route.getPurchasePrice());
		}
	}

}
