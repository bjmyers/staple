package org.psu.init;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.psu.spacetraders.api.MarketplaceClient;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.api.SpaceTradersUtils;
import org.psu.spacetraders.api.WaypointsClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.DataWrapper.WrapperMetadata;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Trait;
import org.psu.spacetraders.dto.Waypoint;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
@ApplicationScoped
public class SystemBuilder {

	private int limit;
	private RequestThrottler throttler;
	private MarketplaceClient marketClient;
	private WaypointsClient waypointsClient;

	@Inject
	public SystemBuilder(@ConfigProperty(name = "app.max-items-per-page") final int limit,
			final RequestThrottler requestThrottler, @RestClient MarketplaceClient marketClient,
			@RestClient WaypointsClient waypointsClient) {
		this.limit = limit;
		this.throttler = requestThrottler;
		this.marketClient = marketClient;
		this.waypointsClient = waypointsClient;

	}

    /**
     * @param systemId The ID of the system
     * @return All of the {@link Waypoint}s in the system
     */
	public List<Waypoint> gatherWaypoints(final String systemId) {
		final DataWrapper<List<Waypoint>> initialPage = throttler
				.throttle(() -> waypointsClient.getWaypoints(systemId, limit, 1));
		log.info("Gathered waypoint page 1");

		final WrapperMetadata metaData = initialPage.getMeta();
		final int numPages = SpaceTradersUtils.getTotalNumPages(metaData);

		final List<Waypoint> waypoints = new ArrayList<>(metaData.getTotal());
		waypoints.addAll(initialPage.getData());

		for (int i = 2; i < numPages + 1; i++) {
			// Need to make the page final to give it to the throttler
			final int page = i;

			final DataWrapper<List<Waypoint>> nextPage = throttler
					.throttle(() -> waypointsClient.getWaypoints(systemId, limit, page));
			log.infof("Gathered waypoint page %s", i);
			waypoints.addAll(nextPage.getData());
		}

		return waypoints;
    }

	/**
	 * @param waypoints The waypoints for which market information should be gathered
	 * @return A map of the {@link Waypoint}s with marketplaces to their
	 *         {@link MarketInfo}. Any waypoints without marketplaces will not be
	 *         present in this map
	 */
	public Map<Waypoint, MarketInfo> gatherMarketInfo(final List<Waypoint> waypoints) {
		return waypoints.stream()
				.filter(w -> w.getTraits().stream().anyMatch(t -> Trait.Type.MARKETPLACE.equals(t.getSymbol())))
				.collect(Collectors.toMap(Function.identity(), waypoint -> throttler.throttle(
						() -> marketClient.getMarketInfo(waypoint.getSystemSymbol(), waypoint.getSymbol()).getData())));
	}

}
