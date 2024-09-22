package org.psu.spacetraders.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * The navigation data contained on a ship, stores its last trip
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipNavigation {

	private String waypointSymbol;
	private ShipRoute route;
	private Status status;
	private FlightMode flightMode;

	public enum Status {
		IN_ORBIT,
		IN_TRANSIT,
		DOCKED,
	}

	public enum FlightMode {
		BURN,
		CRUISE,
		DRIFT,
		STEALTH
	}

}
