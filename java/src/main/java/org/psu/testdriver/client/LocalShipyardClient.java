package org.psu.testdriver.client;

import java.util.List;

import org.psu.spacetraders.api.ShipyardClient;
import org.psu.spacetraders.dto.Agent;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipPurchaseRequest;
import org.psu.spacetraders.dto.ShipPurchaseResponse;
import org.psu.spacetraders.dto.ShipyardResponse;
import org.psu.spacetraders.dto.ShipyardResponse.ShipTypeContainer;
import org.psu.spacetraders.dto.Trait;
import org.psu.spacetraders.dto.Transaction;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testdriver.LocalAgentManager;
import org.psu.testdriver.LocalShipManager;
import org.psu.testdriver.LocalShipyardManager;
import org.psu.testdriver.LocalWaypointsManager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Local version of the {@link ShipyardClient}
 */
@ApplicationScoped
public class LocalShipyardClient implements ShipyardClient {

	@Inject
	private LocalAgentManager agentManager;

	@Inject
	private LocalWaypointsManager waypointsManager;

	@Inject
	private LocalShipManager shipManager;

	@Inject
	private LocalShipyardManager shipyardManager;

	@Override
	public DataWrapper<ShipyardResponse> getShipyardData(String systemId, String waypointId) {

		final Waypoint way = waypointsManager.getWaypoint(waypointId);
		if (way.getTraits().contains(Trait.SHIPYARD)) {
			final List<ShipTypeContainer> typeContainers = shipyardManager.getShipTypes().stream()
					.map(ShipTypeContainer::new).toList();
			final ShipyardResponse shipyardResponse = new ShipyardResponse(waypointId, typeContainers);
			return new DataWrapper<ShipyardResponse>(shipyardResponse, null);
		}

		throw new IllegalArgumentException("Waypoint is not a shipyard");
	}

	@Override
	public DataWrapper<ShipPurchaseResponse> purchaseShip(ShipPurchaseRequest request) {

		final Ship ship = shipyardManager.getShipOfType(request.getShipType());

		if (ship == null) {
			throw new IllegalArgumentException("Invalid Ship Type");
		}

		// Make a new ID
		final String shipId = "Shippy-" + Integer.toString(shipManager.getShips().size() + 1);
		ship.setSymbol(shipId);

		shipManager.addShip(ship);

		// Ships all cost 100k
		final int shipPrice = 100_000;
		final Agent agent = agentManager.getAgent();
		final int newCreditTotal = agent.getCredits() - shipPrice;
		agent.setCredits(newCreditTotal);

		final Transaction transaction = new Transaction(request.getWaypointSymbol(), null, 1, shipPrice);

		final ShipPurchaseResponse response = new ShipPurchaseResponse(agent, ship, transaction);
		return new DataWrapper<ShipPurchaseResponse>(response, null);
	}

}
