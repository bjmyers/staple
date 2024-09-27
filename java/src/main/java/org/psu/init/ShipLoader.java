package org.psu.init;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.psu.shiporchestrator.ShipRoleManager;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.api.ShipsClient;
import org.psu.spacetraders.api.SpaceTradersUtils;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.DataWrapper.WrapperMetadata;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipNavigation;
import org.psu.spacetraders.dto.Waypoint;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Loads the user's ships
 */
@JBossLog
@ApplicationScoped
public class ShipLoader {

	private int limit;
	private final RequestThrottler throttler;
	private final SystemBuilder systemBuilder;
	private final ShipsClient shipsClient;
	private final ShipRoleManager shipRoleManager;

	@Inject
	public ShipLoader(@ConfigProperty(name = "app.max-items-per-page") final int limit,
			final RequestThrottler throttler, final SystemBuilder systemBuilder,
			@RestClient final ShipsClient shipsClient, final ShipRoleManager shipRoleManager) {
		this.limit = limit;
		this.throttler = throttler;
		this.systemBuilder = systemBuilder;
		this.shipsClient = shipsClient;
		this.shipRoleManager = shipRoleManager;
	}

	/**
	 * This method queries the space traders API for system information
	 */
    void onStartup(@Observes StartupEvent event) {
    	log.info("Initializing the Ships");

		final List<Ship> ships = gatherShips();

		ships.forEach(s -> log.infof("Found ship %s with type %s", s.getSymbol(), shipRoleManager.determineRole(s)));

		final Map<String, Long> shipCountBySystem = ships.stream().map(Ship::getNav)
				.collect(Collectors.groupingBy(ShipNavigation::getSystemSymbol, Collectors.counting()));

		final Optional<Entry<String, Long>> primarySystem = shipCountBySystem.entrySet().stream()
				.max(Map.Entry.comparingByValue());

		if (primarySystem.isEmpty()) {
			// User has no ships
			throw new IllegalStateException(
					"No ships found for user, STAPLE requires that the user owns at least one ship");
		}
		final String primarySystemId = primarySystem.get().getKey();
		log.infof("Determined Primary System: %s", primarySystemId);

    	final List<Waypoint> systemWaypoints = systemBuilder.gatherWaypoints(primarySystemId);

    	log.infof("Found %s Waypoints", systemWaypoints.size());

    	log.info("Gathering Market Info");
    	final Map<Waypoint, MarketInfo> marketInfo = systemBuilder.gatherMarketInfo(systemWaypoints);
    	log.infof("Found Market Info for %s marketplaces", marketInfo.size());
	}

    public List<Ship> gatherShips() {
		final DataWrapper<List<Ship>> initialPage = throttler.throttle(() -> shipsClient.getShips(limit, 1));
		log.info("Gathered ship page 1");

		final WrapperMetadata metaData = initialPage.getMeta();
		final int numPages = SpaceTradersUtils.getTotalNumPages(metaData);

		final List<Ship> ships = new ArrayList<>(metaData.getTotal());
		ships.addAll(initialPage.getData());

		for (int i = 2; i < numPages + 1; i++) {
			// Make this final so it can be given to the throttler
			final int page = i;

			final DataWrapper<List<Ship>> nextPage = throttler.throttle(() -> shipsClient.getShips(limit, page));
			log.infof("Gathered ship page %s", i);
			ships.addAll(nextPage.getData());
		}

		return ships;
    }

}
