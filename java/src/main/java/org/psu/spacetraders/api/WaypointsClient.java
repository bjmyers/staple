package org.psu.spacetraders.api;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

/**
 * Class to query the space traders API for waypoint information in a system
 */
@Dependent
@RegisterRestClient(configKey="spacetraders-api")
public interface WaypointsClient {

	@GET
	@Path("/v2/systems/{systemId}/waypoints")
	public String getWaypoints(@PathParam("systemId") String system);

}
