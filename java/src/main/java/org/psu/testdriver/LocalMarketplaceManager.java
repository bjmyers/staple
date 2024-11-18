package org.psu.testdriver;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.psu.spacetraders.dto.MarketInfo;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.jbosslog.JBossLog;

/**
 * Manages marketplaces during local running
 */
@JBossLog
@ApplicationScoped
public class LocalMarketplaceManager {

	private Map<String, MarketInfo> marketsById;

	public LocalMarketplaceManager() {
		this.marketsById = null;
	}

	public MarketInfo getMarketInfo(final String symbol) {
		if (this.marketsById == null) {
			loadMarketInfo();
		}
		//TODO: Determine if a ship is at this market. If not, strip off the tradeGoods
		return this.marketsById.get(symbol);
	}

	private void loadMarketInfo() {
		this.marketsById = LocalResourceLoader.loadResourceList("/testDriverData/marketInfo.json", MarketInfo.class)
				.stream().collect(Collectors.toMap(MarketInfo::getSymbol, Function.identity()));

		log.infof("Local Marketplace Manager loaded %s markets", this.marketsById.size());
	}

}
