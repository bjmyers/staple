package org.psu.spacetraders.api;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.Ship;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

/**
 * Class to query the space traders API for a user's ships
 */
@Dependent
@RegisterRestClient(configKey="spacetraders-api")
public interface ShipsClient {

	@GET
	@Path("/v2/my/ships")
	public DataWrapper<List<Ship>> getShips(@QueryParam("limit") int limit, @QueryParam("page") int page);

}
