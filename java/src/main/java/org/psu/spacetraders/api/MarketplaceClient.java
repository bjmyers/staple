package org.psu.spacetraders.api;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.RefuelResponse;
import org.psu.spacetraders.dto.TradeRequest;
import org.psu.spacetraders.dto.TradeResponse;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

/**
 * Rest client to view a market's information. Note that users are expected to
 * use a {@link MarketplaceRequester} for requests to buy or sell goods
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

	/**
	 * Sells goods from a ship's cargo
	 * @param shipId The ship ID
	 * @param tradeRequest Contains the product and quantity to sell
	 * @return a wrapped {@link TradeResponse}
	 */
	@POST
	@Path("/v2/my/ships/{shipId}/sell")
	public DataWrapper<TradeResponse> sell(@PathParam("shipId") String shipId, TradeRequest tradeRequest);

	/**
	 * Purchases goods from a market
	 * @param shipId The ship ID
	 * @param tradeRequest Contains the product and quantity to purchase
	 * @return a wrapped {@link TradeResponse}
	 */
	@POST
	@Path("/v2/my/ships/{shipId}/purchase")
	public DataWrapper<TradeResponse> purchase(@PathParam("shipId") String shipId, TradeRequest tradeRequest);

	/**
	 * Refuels a ship, must be docked at a waypoint which sells fuel
	 * @param shipId The ship ID
	 * @return a wrapped {@link RefuelResponse}
	 */
	@POST
	@Path("/v2/my/ships/{shipId}/refuel")
	public DataWrapper<RefuelResponse> refuel(@PathParam("shipId") String shipId);

}
