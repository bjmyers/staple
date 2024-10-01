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
	 * Record containing the current and maximum fuel values
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static record FuelStatus(int current, int capacity) {};

	/**
	 * @param waypoint a {@link Waypoint}
	 * @return the distance of the ship to the waypoint
	 */
	public double distTo(final Waypoint waypoint) {
		final RoutePoint shipPosition = this.nav.getRoute().getDestination();
		return Math.sqrt(Math.pow(shipPosition.getX() - waypoint.getX(), 2)
				+ Math.pow(shipPosition.getY() - waypoint.getY(), 2));
	}

}
