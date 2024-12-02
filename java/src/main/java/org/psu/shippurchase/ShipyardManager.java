package org.psu.shippurchase;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.psu.spacetraders.api.AccountManager;
import org.psu.spacetraders.api.ClientProducer;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.api.ShipyardClient;
import org.psu.spacetraders.dto.ShipPurchaseRequest;
import org.psu.spacetraders.dto.ShipPurchaseResponse;
import org.psu.spacetraders.dto.ShipType;
import org.psu.spacetraders.dto.ShipyardResponse;
import org.psu.spacetraders.dto.ShipyardResponse.ShipTypeContainer;
import org.psu.spacetraders.dto.Trait;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.websocket.WebsocketReporter;

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
	private AccountManager accountManager;
	private WebsocketReporter websocketReporter;

	private Map<Waypoint, List<ShipType>> shipsByShipyard;

	@Inject
	public ShipyardManager(final ClientProducer clientProducer, final RequestThrottler requestThrottler,
			final AccountManager accountManager, final WebsocketReporter websocketReporter) {
		this.shipyardClient = clientProducer.produceShipyardClient();
		this.requestThrottler = requestThrottler;
		this.accountManager = accountManager;
		this.websocketReporter = websocketReporter;
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

		final Set<ShipType> allTypes = this.shipsByShipyard.values().stream().flatMap(Collection::stream)
				.collect(Collectors.toSet());
		websocketReporter.addShipTypes(allTypes);

		log.infof("Loaded Shipyard Data for %s shipyards", this.shipsByShipyard.size());
	}

	public List<Waypoint> getShipyardsWhichSell(final ShipType shipType) {
		return this.shipsByShipyard.entrySet().stream().filter(e -> e.getValue().contains(shipType)).map(Entry::getKey)
				.toList();
	}

	public ShipPurchaseResponse purchaseShip(final ShipPurchaseRequest purchaseRequest) {
		final ShipPurchaseResponse response = this.requestThrottler
				.throttle(() -> shipyardClient.purchaseShip(purchaseRequest)).getData();
		accountManager.updateAgent(response.getAgent());
		log.infof("Purchased Ship %s", response.getShip().getSymbol());
		return response;
	}

}
