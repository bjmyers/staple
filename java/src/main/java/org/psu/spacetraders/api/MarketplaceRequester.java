package org.psu.spacetraders.api;

import java.time.Duration;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.psu.spacetraders.dto.CargoItem;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.RefuelResponse;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.TradeRequest;
import org.psu.spacetraders.dto.TradeResponse;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.trademanager.MarketplaceManager;
import org.psu.websocket.WebsocketReporter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Wrapper for the {@link MarketplaceClient} which automatically updates the
 * {@link AccountManager}
 */
@JBossLog
@ApplicationScoped
public class MarketplaceRequester {

	private Duration marketUpdateDelay;
	private MarketplaceClient marketplaceClient;
	private RequestThrottler throttler;
	private AccountManager accountManager;
	private MarketplaceManager marketplaceManager;
	private NavigationHelper navHelper;
	private WebsocketReporter websocketReporter;

	@Inject
	public MarketplaceRequester(@ConfigProperty(name = "app.marketupdate-delay-ms") final int marketUpdateDelay,
			final ClientProducer clientProducer, final RequestThrottler throttler,
			final AccountManager accountManager, final MarketplaceManager marketplaceManager,
			final NavigationHelper navHelper, final WebsocketReporter websocketReporter) {
		this.marketUpdateDelay = Duration.ofMillis(marketUpdateDelay);
		this.marketplaceClient = clientProducer.produceMarketplaceClient();
		this.throttler = throttler;
		this.accountManager = accountManager;
		this.marketplaceManager = marketplaceManager;
		this.navHelper = navHelper;
		this.websocketReporter = websocketReporter;
	}

	/**
	 * Sells goods from a ship's cargo
	 *
	 * @param ship         The ship, its cargo will be modified by this call
	 * @param tradeRequest Contains the product and quantity to sell
	 * @return a {@link TradeResponse}
	 */
	public TradeResponse sell(final Ship ship, final TradeRequest tradeRequest) {
		final TradeResponse response = throttler.throttle(() -> marketplaceClient.sell(ship.getSymbol(), tradeRequest))
				.getData();
		ship.setCargo(response.getCargo());
		this.accountManager.updateAgent(response.getAgent());
		return response;
	}

	/**
	 * Purchases goods from a marketplace
	 *
	 * @param ship         The ship, its cargo will be modified by this call
	 * @param tradeRequest Contains the product and quantity to purchase
	 * @return a {@link TradeResponse}
	 */
	public TradeResponse purchase(final Ship ship, final TradeRequest tradeRequest) {
		final TradeResponse response = throttler
				.throttle(() -> marketplaceClient.purchase(ship.getSymbol(), tradeRequest)).getData();
		ship.setCargo(response.getCargo());
		this.accountManager.updateAgent(response.getAgent());
		return response;
	}

	/**
	 * Refuels a ship, must be docked at a waypoint which sells fuel
	 *
	 * @param ship         The ship, its fuel status will be modified by this call
	 * @return a wrapped {@link RefuelResponse}
	 */
	public RefuelResponse refuel(final Ship ship) {
		final RefuelResponse response = throttler.throttle(() -> marketplaceClient.refuel(ship.getSymbol())).getData();
		ship.setFuel(response.getFuel());
		this.accountManager.updateAgent(response.getAgent());
		return response;
	}

	/**
	 * Docks the ship, sells the given items, and then refuels if the market sells
	 * fuel
	 *
	 * @param ship        The ship
	 * @param waypoint    The waypoint at which to sell the goods, assumes the ship
	 *                    has finished traveling to it
	 * @param itemsToSell The items to sell, they must be in the ship's cargo bay
	 * @return The profit from selling items (amount gained from selling minus cost of refueling)
	 */
	public Integer dockAndSellItems(final Ship ship, final Waypoint waypoint, final List<CargoItem> itemsToSell) {
		final String shipId = ship.getSymbol();

		navHelper.dock(ship);

		// TODO: Remove this when space traders fixes their bug, it takes a while for markets to realize
		// a ship is docked at them
		try {
			Thread.sleep(marketUpdateDelay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Force an update so we know most current prices
		final MarketInfo market = marketplaceManager.updateMarketInfo(waypoint);
		final List<TradeRequest> sellRequests = market.buildSellRequests(itemsToSell);

		int totalCredits = 0;
		for (final TradeRequest sellRequest : sellRequests) {
			final TradeResponse tradeResp = sell(ship, sellRequest);
			totalCredits += tradeResp.getTransaction().getTotalPrice();
		}
		final String message = String.format("Sold goods from ship %s for a total of %s credits", shipId, totalCredits);
		websocketReporter.fireShipEvent(shipId, message);
		log.info(message);

		if (market.sellsProduct(Product.FUEL)) {
			final RefuelResponse refuelResponse = refuel(ship);
			totalCredits -= refuelResponse.getTransaction().getTotalPrice();
			log.infof("Refueled ship %s", shipId);
		}
		else {
			log.warnf("Unable to refuel ship %s at current waypoint", shipId);
		}
		return totalCredits;
	}

}
