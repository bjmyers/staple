package org.psu.spacetraders.api;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.ShipyardResponse;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

/**
 * Class to query the space traders API for a shipyard's properties
 */
@Dependent
@RegisterRestClient(configKey="spacetraders-api")
public interface ShipyardClient {

	@GET
	@Path("/v2/systems/{systemId}/waypoints/{waypointId}/shipyard")
	public DataWrapper<ShipyardResponse> getShipyardData(@PathParam("systemId") String systemId,
			@PathParam("waypointId") String waypointId);

}
