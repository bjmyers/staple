package org.psu.spacetraders.api;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.DockResponse;
import org.psu.spacetraders.dto.NavigationRequest;
import org.psu.spacetraders.dto.NavigationResponse;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

/**
 * Rest client to navigate with a ship
 */
@Dependent
@RegisterRestClient(configKey="spacetraders-api")
public interface NavigationClient {

	/**
	 * Commands a ship to orbit its current waypoint
	 * @param shipId The identifier of the ship
	 * @return a wrapped {@link DockResponse}
	 */
	@POST
	@Path("/v2/my/ships/{shipId}/orbit")
	public DataWrapper<DockResponse> orbit(@PathParam("shipId") String shipId);

	/**
	 * Commands a ship to dock at its current waypoint
	 * @param shipId The identifier of the ship
	 * @return a wrapped {@link DockResponse}
	 */
	@POST
	@Path("/v2/my/ships/{shipId}/dock")
	public DataWrapper<DockResponse> dock(@PathParam("shipId") String shipId);

	/**
	 * Commands a ship to start navigation
	 * @param shipId The identifier of the ship
	 * @param navRequest The request for navigation
	 * @return a wrapped {@link NavigationResponse}, which contains the time of arrival
	 */
	@POST
	@Path("/v2/my/ships/{shipId}/navigate")
	public DataWrapper<NavigationResponse> navigate(@PathParam("shipId") String shipId, NavigationRequest navRequest);

}
