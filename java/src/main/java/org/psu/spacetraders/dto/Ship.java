package org.psu.spacetraders.dto;

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
	private FuelStatus fuel;
	private Cargo cargo;

	/**
	 * Record containing the current and maximum fuel values
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static record FuelStatus(int current, int capacity) {};

}
