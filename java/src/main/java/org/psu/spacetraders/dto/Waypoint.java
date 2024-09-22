package org.psu.spacetraders.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Represents a single waypoint in a system
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Waypoint {

	private String symbol;
	private WaypointType type;
	private List<Orbital> orbitals;
	private List<Trait> traits;
	private int x;
	private int y;

}
