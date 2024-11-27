package org.psu.spacetraders.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * The response from a shipyard query
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipyardResponse {

	private String symbol;
	private List<ShipTypeContainer> shipTypes;

	/**
	 * Wrapper for the ship type enum
	 */
	@Data
	@AllArgsConstructor
	@RequiredArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ShipTypeContainer {

		private ShipType type;
	}

}
