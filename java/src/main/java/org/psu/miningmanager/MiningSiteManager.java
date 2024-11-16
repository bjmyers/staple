package org.psu.miningmanager;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.psu.navigation.NavigationPath;
import org.psu.navigation.RefuelPathCalculator;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.Waypoint;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Stores the waypoints with valuable materials
 */
@ApplicationScoped
public class MiningSiteManager {

	private RefuelPathCalculator pathCalculator;
	private Map<String, Waypoint> miningSitesById;

	@Inject
	public MiningSiteManager(final RefuelPathCalculator pathCalculator) {
		this.pathCalculator = pathCalculator;
		this.miningSitesById = new HashMap<>();
	}

	/**
	 * Loads the manager
	 *
	 * @param waypoints all of the waypoints in the system, the waypoints which are
	 *                  mining sites will be pulled out and stored
	 */
	public void addSites(final List<Waypoint> waypoints) {
		waypoints.stream().filter(w -> w.getTraits().stream().anyMatch(t -> t.getSymbol().isValuable()))
				.forEach(w -> miningSitesById.put(w.getSymbol(), w));
	}

	/**
	 * @param waypointId The waypoint symbol
	 * @return The waypoint with that symbol is a mining site and has been loaded
	 *         into this manager, null otherwise
	 */
	public Waypoint getMiningSite(final String waypointId) {
		return this.miningSitesById.get(waypointId);
	}

	/**
	 * @param ship a ship
	 * @return The mining site closest to the ship's current position, empty if
	 *         there are no mining sites loaded or none are reachable by the ship
	 */
	public Optional<Deque<Waypoint>> getClosestMiningSite(final Ship ship) {
		return this.miningSitesById.values().stream().map(w -> this.pathCalculator.determineShortestRoute(ship, w))
				.filter(Objects::nonNull).min(Comparator.comparing(path -> path.getLength()))
				.map(NavigationPath::getWaypoints);
	}

}
