package org.psu.trademanager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.psu.spacetraders.api.MarketplaceClient;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.Waypoint;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Manages the current state of information regarding marketplaces in the system;
 */
@ApplicationScoped
public class MarketplaceManager {

	private RequestThrottler throttler;
	private MarketplaceClient marketClient;

	private Map<Waypoint, MarketInfo> marketData;

	@Inject
	public MarketplaceManager(final RequestThrottler throttler, @RestClient final MarketplaceClient marketClient) {
		this.throttler = throttler;
		this.marketClient = marketClient;

		this.marketData = new HashMap<>();
	}

	/**
	 * Updates the marketplace information for the system.
	 * @param currentInfo The {@link MarketInfo}, grouped by {@link Waypoint}
	 * Note, any waypoints not in currentInfo will be retained by the manager
	 */
	public void updateMarketData(final Map<Waypoint, MarketInfo> currentInfo) {
		marketData.putAll(currentInfo);
	}

	/**
	 * @param waypoint a {@link Waypoint}
	 * @return the {@link MarketInfo} for this waypoint pulled directly from the
	 *         space traders api, this method will not use the cached result. This
	 *         method will update the cached value for this waypoint
	 */
	public MarketInfo updateMarketInfo(final Waypoint waypoint) {
		final MarketInfo marketInfo = throttler.throttle(
				() -> marketClient.getMarketInfo(waypoint.getSystemSymbol(), waypoint.getSymbol()).getData());
		marketData.put(waypoint, marketInfo);
		return marketInfo;
	}

	/**
	 * @param waypoint a {@link Waypoint}
	 * @return the most up to date {@link MarketInfo} for the waypoint, never null,
	 *         will query the space traders API if no current market data exists
	 */
	public MarketInfo getMarketInfo(final Waypoint waypoint) {
		final MarketInfo marketInfo = marketData.get(waypoint);
		return marketInfo == null ? updateMarketInfo(waypoint) : marketInfo;
	}

	/**
	 * @param waypointId The waypointID
	 * @return An optional entry of the waypoint and marketInfo, will be present if
	 *         a waypoint with a matching ID is found, will be absent if there is no
	 *         matching waypoint
	 * @apiNote It is recommended to use getMarketInfo when possible
	 */
	public Optional<Entry<Waypoint, MarketInfo>> getMarketInfoById(final String waypointId) {
		return this.marketData.entrySet().stream().filter(entry -> entry.getKey().getSymbol().equals(waypointId))
				.findAny();
	}

	/**
	 * @return all current market info for the current system, grouped by waypoint
	 */
	public Map<Waypoint, MarketInfo> getAllMarketInfo() {
		return marketData;
	}

	/**
	 * @param ship    The {@link Ship} which has the product
	 * @param product The {@link Product} being sold
	 * @return The closest {@link Waypoint} to the ship which sells the product
	 */
	public Optional<Waypoint> getClosestImport(final Ship ship, final Product product) {
		return this.marketData.entrySet().stream().filter(entry -> entry.getValue().getImports().contains(product))
				.min((entry1, entry2) -> Double.compare(ship.distTo(entry1.getKey()), ship.distTo(entry2.getKey())))
				.map(Entry::getKey);
	}

}
