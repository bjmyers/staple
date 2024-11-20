package org.psu.spacetraders.dto;

import java.util.List;

import org.psu.spacetraders.dto.ShipRoute.RoutePoint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Class representing a ship
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ship {

	private String symbol;
	private ShipNavigation nav;
	private List<ShipComponent> modules;
	private List<ShipComponent> mounts;
	private FuelStatus fuel;
	private Cargo cargo;

	/**
	 * @param waypoint a {@link Waypoint}
	 * @return the distance of the ship to the waypoint
	 */
	public double distTo(final Waypoint waypoint) {
		final RoutePoint shipPosition = this.nav.getRoute().getDestination();
		return Math.sqrt(Math.pow(shipPosition.getX() - waypoint.getX(), 2)
				+ Math.pow(shipPosition.getY() - waypoint.getY(), 2));
	}

	/**
	 * @param waypoint a waypoint
	 * @return true if this ship can travel to the waypoint given its fuel capacity
	 */
	public boolean canTravelTo(final Waypoint waypoint) {
		return this.fuel.current() > this.distTo(waypoint);
	}

	/**
	 * @return The amount of open cargo space on the ship
	 */
	public int getRemainingCargo() {
		return this.cargo.getCapacity() - this.cargo.getUnits();
	}

}
