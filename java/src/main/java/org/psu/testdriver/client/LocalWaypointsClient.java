package org.psu.testdriver.client;

import java.util.List;

import org.psu.spacetraders.api.WaypointsClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.DataWrapper.WrapperMetadata;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testdriver.LocalWaypointsManager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Local version of the {@link WaypointsClient}
 */
@ApplicationScoped
public class LocalWaypointsClient implements WaypointsClient {

	@Inject
	private LocalWaypointsManager waypointsManager;

	@Override
	public DataWrapper<List<Waypoint>> getWaypoints(String system, int limit, int page) {
		final List<Waypoint> waypoints = waypointsManager.getWaypoints();
		final WrapperMetadata metadata = new WrapperMetadata(waypoints.size(), page, limit);
		if (page == 1) {
			return new DataWrapper<>(waypoints, metadata);
		}
		return new DataWrapper<>(List.of(), metadata);
	}

}
