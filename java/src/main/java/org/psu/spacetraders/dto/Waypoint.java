package org.psu.spacetraders.dto;

import lombok.Data;

/**
 * Represents a single waypoint in a system
 */
@Data
public class Waypoint {

	private final String symbol;
	private final String type;
	private final int x;
	private final int y;

}
