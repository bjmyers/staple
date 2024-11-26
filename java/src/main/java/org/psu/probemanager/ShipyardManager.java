package org.psu.probemanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.psu.spacetraders.api.ClientProducer;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.api.ShipyardClient;
import org.psu.spacetraders.dto.ShipType;
import org.psu.spacetraders.dto.ShipyardResponse;
import org.psu.spacetraders.dto.ShipyardResponse.ShipTypeContainer;
import org.psu.spacetraders.dto.Trait;
import org.psu.spacetraders.dto.Waypoint;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Manages data for shipyards
 */
@JBossLog
@ApplicationScoped
public class ShipyardManager {

	private ShipyardClient shipyardClient;
	private RequestThrottler requestThrottler;

	private Map<Waypoint, List<ShipType>> shipsByShipyard;

	@Inject
	public ShipyardManager(final ClientProducer clientProducer, final RequestThrottler requestThrottler) {
		this.shipyardClient = clientProducer.produceShipyardClient();
		this.requestThrottler = requestThrottler;
		this.shipsByShipyard = null;
	}

	public void loadData(final List<Waypoint> systemWaypoints) {

		log.info("Loading Shipyard Data");

		final List<Waypoint> shipyards = systemWaypoints.stream().filter(w -> w.getTraits().contains(Trait.SHIPYARD))
				.toList();

		this.shipsByShipyard = new HashMap<>();
		for (final Waypoint shipyard : shipyards) {
			final ShipyardResponse shipyardResponse = requestThrottler.throttle(
					() -> shipyardClient.getShipyardData(shipyard.getSystemSymbol(), shipyard.getSymbol()).getData());
			final List<ShipType> shipTypes = shipyardResponse.getShipTypes().stream().map(ShipTypeContainer::getType).toList();
			this.shipsByShipyard.put(shipyard, shipTypes);
		}

		log.infof("Loaded Shipyard Data for %s shipyards", this.shipsByShipyard.size());
	}


}
