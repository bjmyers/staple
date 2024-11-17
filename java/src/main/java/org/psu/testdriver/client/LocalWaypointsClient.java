package org.psu.testdriver.client;

import java.util.List;

import org.psu.spacetraders.api.WaypointsClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.Waypoint;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Local version of the {@link WaypointsClient}
 */
@ApplicationScoped
public class LocalWaypointsClient implements WaypointsClient {

	@Override
	public DataWrapper<List<Waypoint>> getWaypoints(String system, int limit, int page) {
		return null;
	}

}
