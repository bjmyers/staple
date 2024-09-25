package org.psu.init;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.psu.spacetraders.api.ShipsClient;
import org.psu.spacetraders.api.SpaceTradersUtils;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.DataWrapper.WrapperMetadata;
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
	private final SystemBuilder systemBuilder;
	private final ShipsClient shipsClient;

	@Inject
	public ShipLoader(@ConfigProperty(name = "app.max-items-per-page") final int limit,
			final SystemBuilder systemBuilder, @RestClient final ShipsClient shipsClient) {
		this.limit = limit;
		this.systemBuilder = systemBuilder;
		this.shipsClient = shipsClient;
	}

	/**
	 * This method queries the space traders API for system information
	 */
    void onStartup(@Observes StartupEvent event) {
    	log.info("Initializing the Ships");

		final List<Ship> ships = gatherShips();

		log.infof("Found %s ships", ships.size());

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
	}

    public List<Ship> gatherShips() {
		final DataWrapper<List<Ship>> initialPage = shipsClient.getShips(limit, 1);
		log.info("Gathered ship page 1");

		final WrapperMetadata metaData = initialPage.getMeta();
		final int numPages = SpaceTradersUtils.getTotalNumPages(metaData);

		final List<Ship> ships = new ArrayList<>(metaData.getTotal());
		ships.addAll(initialPage.getData());

		for (int i = 2; i < numPages + 1; i++) {
			//TODO: Remove this when the throttler exists
			try {
				Thread.sleep(Duration.ofSeconds(1));
			} catch (InterruptedException e) {
				log.error("Sleep between pages interrupted");
			}

			final DataWrapper<List<Ship>> nextPage = shipsClient.getShips(limit, i);
			log.infof("Gathered waypoint page %s", i);
			ships.addAll(nextPage.getData());
		}

		return ships;
    }

}
