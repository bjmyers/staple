package org.psu.testdriver;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipType;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Responsible for loading the ships for sale when in local mode
 */
@ApplicationScoped
public class LocalShipyardManager {

	Map<ShipType, Ship> shipsByType;

	@Inject
	public LocalShipyardManager() {
		this.shipsByType = null;
	}

	public Ship getShipOfType(final ShipType type) {
		// Want to load every time so we're not returning the same reference multiple
		// times
		loadShips();
		return this.shipsByType.get(type);
	}

	public Collection<ShipType> getShipTypes() {
		if (this.shipsByType == null) {
			loadShips();
		}
		return this.shipsByType.keySet();
	}

	private void loadShips() {
		final Ship tradeShip = LocalResourceLoader.loadResource("/testDriverData/tradeShipPurchase.json", Ship.class);
		final Ship miningShip = LocalResourceLoader.loadResource("/testDriverData/miningShipPurchase.json", Ship.class);

		this.shipsByType = new HashMap<>();
		this.shipsByType.put(ShipType.SHIP_LIGHT_HAULER, tradeShip);
		this.shipsByType.put(ShipType.SHIP_MINING_DRONE, miningShip);
	}

}
