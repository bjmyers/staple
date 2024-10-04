package org.psu.spacetraders.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Represents a single waypoint in a system
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Waypoint {

	private String systemSymbol;
	private String symbol;
	private WaypointType type;
	private List<Orbital> orbitals;
	private List<Trait> traits;
	private int x;
	private int y;

	/**
	 * @param waypoint another {@link Waypoint}
	 * @return the distance between this waypoint and the given waypoint
	 */
	public double distTo(final Waypoint waypoint) {
		return Math.sqrt(Math.pow(this.getX() - waypoint.getX(), 2)
				+ Math.pow(this.getY() - waypoint.getY(), 2));
	}

}
