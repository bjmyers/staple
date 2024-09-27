package org.psu.spacetraders.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * A part of a ship, either a module or a mount
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipComponent {

	private String symbol;

	public boolean isMiningLaser() {
		return symbol.contains("MINING_LASER");
	}

	public boolean isCargoHold() {
		return symbol.contains("CARGO_HOLD");
	}

}
