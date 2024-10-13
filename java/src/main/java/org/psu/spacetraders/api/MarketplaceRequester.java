package org.psu.spacetraders.api;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.psu.spacetraders.dto.RefuelResponse;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.TradeRequest;
import org.psu.spacetraders.dto.TradeResponse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Wrapper for the {@link MarketplaceClient} which automatically updates the
 * {@link AccountManager}
 */
@ApplicationScoped
public class MarketplaceRequester {

	private MarketplaceClient marketplaceClient;
	private RequestThrottler throttler;
	private AccountManager accountManager;

	@Inject
	public MarketplaceRequester(@RestClient MarketplaceClient marketplaceClient,
			final RequestThrottler throttler, final AccountManager accountManager) {
		this.marketplaceClient = marketplaceClient;
		this.throttler = throttler;
		this.accountManager = accountManager;
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

}
