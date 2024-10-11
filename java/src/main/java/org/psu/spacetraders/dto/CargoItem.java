package org.psu.spacetraders.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents an item in the cargo hold of a ship
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CargoItem(String symbol, int units) {

}
