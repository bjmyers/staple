package org.psu.trademanager.dto;

import java.util.List;

import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.Waypoint;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A trade route denotes a route where given goods can be traded from one
 * waypoint which exports them, and another which imports them
 */
@Data
@AllArgsConstructor
public class TradeRoute {

	private final Waypoint exportWaypoint;
	private final Waypoint importWaypoint;
	private List<Product> goods;

	/**
	 * Determines if a ship can perform the trade route (on a single tank of fuel)
	 * @param ship
	 * @return
	 */
	public boolean isPossible(final Ship ship) {
		final double totalDist = ship.distTo(exportWaypoint) + exportWaypoint.distTo(importWaypoint);
		return ship.getFuel().current() > totalDist;
	}

	/**
	 * @return The distance between the two waypoints in this trade route
	 */
	public double getDistance() {
		return Math.sqrt(Math.pow(this.exportWaypoint.getX() - this.importWaypoint.getX(), 2) +
				Math.pow(this.exportWaypoint.getY() - this.importWaypoint.getY(), 2));
	}

}
