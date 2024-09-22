package org.psu.spacetraders.dto;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

/**
 * The type of a waypoint
 */
public enum WaypointType {
	ASTEROID,
	ASTEROID_BASE,
	ENGINEERED_ASTEROID,
	FUEL_STATION,
	GAS_GIANT,
	JUMP_GATE,
	MOON,
	ORBITAL_STATION,
	PLANET,
	@JsonEnumDefaultValue
	UNKNOWN
}
