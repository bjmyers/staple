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

	private String symbol;
	private WaypointType type;
	private List<Orbital> orbitals;
	private List<Trait> traits;
	private int x;
	private int y;

}
