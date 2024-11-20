package org.psu.spacetraders.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents an item in the cargo hold of a ship
 */
@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CargoItem {

	private String symbol;
	private int units;

}
