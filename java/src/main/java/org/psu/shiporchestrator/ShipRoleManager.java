package org.psu.shiporchestrator;

import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipComponent;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Determines a ship's role
 */
@ApplicationScoped
public class ShipRoleManager {

	/**
	 * @param ship a ship
	 * @return the proper role of the ship
	 */
	public ShipRole determineRole(final Ship ship) {
		if (ship.getModules().stream().anyMatch(ShipComponent::isMiningLaser)) {
			return ShipRole.MINING;
		}
		return ShipRole.TRADE;
	}

}
