package org.psu.spacetraders.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the cargo in a ship
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Cargo(int capacity, int units, List<CargoItem> inventory) {

}
