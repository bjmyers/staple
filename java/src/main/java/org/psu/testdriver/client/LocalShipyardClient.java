package org.psu.testdriver.client;

import java.util.List;

import org.psu.spacetraders.api.ShipyardClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.ShipPurchaseRequest;
import org.psu.spacetraders.dto.ShipPurchaseResponse;
import org.psu.spacetraders.dto.ShipyardResponse;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Local version of the {@link ShipyardClient}
 */
@ApplicationScoped
public class LocalShipyardClient implements ShipyardClient {

	@Override
	public DataWrapper<ShipyardResponse> getShipyardData(String systemId, String waypointId) {
		final ShipyardResponse shipyardResponse = new ShipyardResponse(waypointId, List.of());
		return new DataWrapper<ShipyardResponse>(shipyardResponse, null);
	}

	@Override
	public DataWrapper<ShipPurchaseResponse> purchaseShip(ShipPurchaseRequest request) {
		return null;
	}

}
