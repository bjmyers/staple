package org.psu.testdriver.client;

import org.psu.spacetraders.api.MarketplaceClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.RefuelResponse;
import org.psu.spacetraders.dto.TradeRequest;
import org.psu.spacetraders.dto.TradeResponse;
import org.psu.testdriver.LocalMarketplaceManager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Local version of the {@link MarketplaceClient}
 */
@ApplicationScoped
public class LocalMarketplaceClient implements MarketplaceClient {

	@Inject
	private LocalMarketplaceManager marketplaceManager;

	@Override
	public DataWrapper<MarketInfo> getMarketInfo(String systemId, String waypointId) {
		return new DataWrapper<>(marketplaceManager.getMarketInfo(waypointId), null);
	}

	@Override
	public DataWrapper<TradeResponse> sell(String shipId, TradeRequest tradeRequest) {
		return null;
	}

	@Override
	public DataWrapper<TradeResponse> purchase(String shipId, TradeRequest tradeRequest) {
		return null;
	}

	@Override
	public DataWrapper<RefuelResponse> refuel(String shipId) {
		return null;
	}

}
