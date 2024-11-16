package org.psu.trademanager.dto;

import java.util.List;
import java.util.Objects;

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
	 * An unknown trade route is one where the prices are not known at either the
	 * export or the import, or both
	 */
	private boolean isKnown;

	public TradeRoute(final Waypoint exportWaypoint, final Waypoint importWaypoint, final List<Product> goods) {
		this.exportWaypoint = exportWaypoint;
		this.importWaypoint = importWaypoint;
		this.goods = goods;
		this.isKnown = false;
	}

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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
			return true;
		}
        if (other == null || getClass() != other.getClass()) {
			return false;
		}
        TradeRoute otherRoute = (TradeRoute) other;
        return Objects.equals(this.exportWaypoint, otherRoute.getExportWaypoint()) &&
               Objects.equals(this.importWaypoint, otherRoute.getImportWaypoint());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.exportWaypoint, this.importWaypoint);
    }

}
