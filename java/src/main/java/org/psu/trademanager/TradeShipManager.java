package org.psu.trademanager;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.psu.spacetraders.api.AccountManager;
import org.psu.spacetraders.api.MarketplaceRequester;
import org.psu.spacetraders.api.NavigationHelper;
import org.psu.spacetraders.dto.CargoItem;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.RefuelResponse;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.TradeRequest;
import org.psu.spacetraders.dto.TradeResponse;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.trademanager.RouteManager.RouteResponse;
import org.psu.trademanager.dto.TradeRoute;
import org.psu.trademanager.dto.TradeShipJob;
import org.psu.trademanager.dto.TradeShipJob.State;
import org.psu.websocket.WebsocketReporter;

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
	private Duration marketUpdateDelay;
	private NavigationHelper navigationHelper;
	private AccountManager accountManager;
	private MarketplaceRequester marketplaceRequester;
	private MarketplaceManager marketplaceManager;
	private RouteManager routeManager;
	private WebsocketReporter websocketReporter;

	@Inject
	public TradeShipManager(@ConfigProperty(name = "app.cooldown-pad-ms") final int navigationPad,
			@ConfigProperty(name = "app.marketupdate-delay-ms") final int marketUpdateDelay,
			final NavigationHelper navigationHelper,
			final AccountManager accountManager, final MarketplaceRequester marketplaceRequester,
			final MarketplaceManager marketplaceManager, final RouteManager routeManager,
			final WebsocketReporter websocketReporter) {
		this.navigationPad = Duration.ofMillis(navigationPad);
		this.marketUpdateDelay = Duration.ofMillis(marketUpdateDelay);
		this.navigationHelper = navigationHelper;
		this.accountManager = accountManager;
		this.marketplaceRequester = marketplaceRequester;
		this.marketplaceManager = marketplaceManager;
		this.routeManager = routeManager;
		this.websocketReporter = websocketReporter;
	}

	public TradeShipJob createJob(final Ship ship) {

		// If the ship has goods in its cargo hold, see if we can sell them
		if (ship.getCargo().getUnits() > 0) {

			// Sorted so that the item with the highest count is first
			final List<CargoItem> items = ship.getCargo().getInventory().stream()
					.sorted((c1, c2) -> Integer.compare(c2.getUnits(), c1.getUnits()))
					.toList();

			// First check the destination to see if it buys anything we've got
			final Optional<Entry<Waypoint, MarketInfo>> optionalDestinationMarketInfo = marketplaceManager
					.getMarketInfoById(ship.getNav().getRoute().getDestination().getSymbol());
			if (optionalDestinationMarketInfo.isPresent()) {
				final Entry<Waypoint, MarketInfo> destinationMarketInfo = optionalDestinationMarketInfo.get();
				final List<String> importProductSymbols = destinationMarketInfo.getValue().getImports().stream()
						.map(Product::getSymbol).toList();
				final List<CargoItem> itemsToSell = items.stream()
						.filter(i -> importProductSymbols.contains(i.getSymbol())).toList();
				if (itemsToSell.size() > 0) {
					// We are able to sell some of our inventory at the destination, build a trade
					// route
					final List<Product> productsToSell = itemsToSell.stream().map(c -> new Product(c.getSymbol()))
							.toList();
					final TradeRoute route = new TradeRoute(null, destinationMarketInfo.getKey(), productsToSell);
					final Queue<Waypoint> destinationWaypoint = new LinkedList<>();
					destinationWaypoint.add(destinationMarketInfo.getKey());
					final TradeShipJob job = new TradeShipJob(ship, route, destinationWaypoint);
					job.setState(State.TRAVELING);
					job.setNextAction(ship.getNav().getRoute().getArrival().plus(navigationPad));
					return job;
				}
			}
		}

		// Nothing in the cargo bay, let's go and make normal trade routes
		final RouteResponse bestRoute = routeManager.getBestRoute(ship);
		return new TradeShipJob(ship, bestRoute.route(), bestRoute.waypoints());
	}

	/**
	 * Performs the next step in a {@link TradeShipJob}
	 * @param job the {@link TradeShipJob}
	 * @return The updated {@link TradeShipJob}, with an updated nextAction time
	 */
	public TradeShipJob manageTradeShip(final TradeShipJob job) {
		final Ship ship = job.getShip();

		// The state of the job indicates what is was doing before it reached the
		// manager
		switch (job.getState()) {
		case NOT_STARTED:

			final Waypoint destination = job.getWaypoints().peek();

			log.infof("Ship %s traveling to waypoint %s", ship.getSymbol(), destination.getSymbol());
			final Instant exportArrival = navigationHelper.navigate(ship, destination);
			job.setNextAction(exportArrival);
			job.getWaypoints().remove();
			job.setState(State.TRAVELING);
			break;
		case TRAVELING:

			if (job.getRoute().getExportWaypoint() != null
					&& ship.getNav().getWaypointSymbol().equals(job.getRoute().getExportWaypoint().getSymbol())) {
				// We're currently at the export waypoint
				purchaseGoods(job);
			}
			if (ship.getNav().getWaypointSymbol().equals(job.getRoute().getImportWaypoint().getSymbol())) {
				// We're currently at the import waypoint
				sellGoods(job);
				// Make a whole new job and return it
				return createJob(ship);
			}

			// So long as we're not at the import waypoint, there is at least one more waypoint to travel to
			final Waypoint travelDestination = job.getWaypoints().peek();

			// The only waypoints in a route should be the ones which sell fuel
			final RefuelResponse refuelResponse = marketplaceRequester.refuel(ship);
			job.modifyProfit(-1 * refuelResponse.getTransaction().getTotalPrice());
			log.infof("Ship %s traveling to waypoint %s", ship.getSymbol(), travelDestination.getSymbol());
			final Instant travelArrival = navigationHelper.navigate(ship, travelDestination);
			job.setNextAction(travelArrival);
			job.getWaypoints().remove();
			break;
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
			Thread.sleep(marketUpdateDelay);
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
			final String message = String.format("Purchased %s unit(s) of %s for %s credits", tradeRequest.getUnits(),
					tradeRequest.getSymbol(), purchaseResponse.getTransaction().getTotalPrice());
			websocketReporter.fireShipEvent(ship.getSymbol(), message);
			log.info(message);
		}
		log.infof("Total Purchase Price: %s", total);
		job.modifyProfit(-1 * total);

		return purchaseRequests;
	}

	private void sellGoods(final TradeShipJob job) {
		final Ship ship = job.getShip();
		final TradeRoute route = job.getRoute();

		final List<String> productsToSell = route.getGoods().stream().map(Product::getSymbol).toList();
		final List<CargoItem> cargoToSell = ship.getCargo().getInventory().stream()
				.filter(c -> productsToSell.contains(c.getSymbol())).toList();

		final Integer sellPrice = marketplaceRequester.dockAndSellItems(ship, route.getImportWaypoint(), cargoToSell);
		job.modifyProfit(sellPrice);
		final String message = String.format("Trade route for ship %s made a total profit of %s", ship.getSymbol(),
				job.getProfit());
		websocketReporter.fireShipEvent(ship.getSymbol(), message);
		log.info(message);
	}

}
