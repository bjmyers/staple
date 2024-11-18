package org.psu.testdriver;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.psu.spacetraders.dto.Waypoint;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Responsible for loading waypoints when in local mode
 */
@JBossLog
@ApplicationScoped
public class LocalWaypointsManager {

	private Map<String, Waypoint> waypointsById;

	@Inject
	public LocalWaypointsManager() {
		this.waypointsById = null;
	}

	public List<Waypoint> getWaypoints() {
		if (this.waypointsById == null) {
			loadWaypoints();
		}
		return this.waypointsById.values().stream().toList();
	}

	private void loadWaypoints() {
		this.waypointsById = LocalResourceLoader.loadResourceList("/testDriverData/waypoints.json", Waypoint.class).stream()
				.collect(Collectors.toMap(Waypoint::getSymbol, Function.identity()));

		log.infof("Local Waypoint Manager loaded %s waypoints", this.waypointsById.size());
	}

}
