package org.psu.spacetraders.api;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.Waypoint;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

/**
 * Class to query the space traders API for waypoint information in a system
 */
@Dependent
@RegisterRestClient(configKey="spacetraders-api")
public interface WaypointsClient {

	/**
	 * Gets the waypoints from a given system
	 * @param system The system name
	 * @param limit The number of waypoints to request
	 * @param page The page to request
	 * @return A wrapped list of {@link Waypoint}s
	 */
	@GET
	@Path("/v2/systems/{systemId}/waypoints")
	public DataWrapper<List<Waypoint>> getWaypoints(@PathParam("systemId") String system,
			@QueryParam("limit") int limit, @QueryParam("page") int page);

}
