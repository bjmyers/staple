package org.psu.spacetraders.api;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.MarketInfo;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

/**
 * Rest client to view a market's information
 */
@Dependent
@RegisterRestClient(configKey="spacetraders-api")
public interface MarketplaceClient {

	/**
	 * Gets the information of a given marketplace
	 *
	 * @param systemId   The ID of the system
	 * @param waypointId The ID of a waypoint with a marketplace
	 * @return A wrapped {@link MarketInfo}
	 */
	@GET
	@Path("/v2/systems/{systemId}/waypoints/{waypointId}/market")
	public DataWrapper<MarketInfo> getMarketInfo(@PathParam("systemId") String systemId,
			@PathParam("waypointId") String waypointId);

}
