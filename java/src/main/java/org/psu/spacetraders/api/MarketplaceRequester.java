package org.psu.spacetraders.api;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.psu.spacetraders.dto.RefuelResponse;
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
	private AccountManager accountManager;

	@Inject
	public MarketplaceRequester(@RestClient MarketplaceClient marketplaceClient,
			final AccountManager accountManager) {
		this.marketplaceClient = marketplaceClient;
		this.accountManager = accountManager;
	}

	/**
	 * Sells goods from a ship's cargo
	 * @param shipId The ship ID
	 * @param tradeRequest Contains the product and quantity to sell
	 * @return a {@link TradeResponse}
	 */
	public TradeResponse sell(final String shipId, final TradeRequest tradeRequest) {
		final TradeResponse response = marketplaceClient.sell(shipId, tradeRequest).getData();
		this.accountManager.updateAgent(response.getAgent());
		return response;
	}

	/**
	 * Purchases goods from a marketplace
	 * @param shipId The ship ID
	 * @param tradeRequest Contains the product and quantity to purchase
	 * @return a {@link TradeResponse}
	 */
	public TradeResponse purchase(final String shipId, final TradeRequest tradeRequest) {
		final TradeResponse response = marketplaceClient.purchase(shipId, tradeRequest).getData();
		this.accountManager.updateAgent(response.getAgent());
		return response;
	}

	/**
	 * Refuels a ship, must be docked at a waypoint which sells fuel
	 * @param shipId The ship ID
	 * @return a wrapped {@link RefuelResponse}
	 */
	public RefuelResponse refuel(final String shipId) {
		final RefuelResponse response = marketplaceClient.refuel(shipId).getData();
		this.accountManager.updateAgent(response.getAgent());
		return response;
	}

}
