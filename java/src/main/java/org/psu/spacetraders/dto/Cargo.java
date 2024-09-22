package org.psu.spacetraders.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the cargo in a ship
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Cargo(int capacity, int units) {

}
