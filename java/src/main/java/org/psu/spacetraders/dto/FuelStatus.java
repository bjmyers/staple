package org.psu.spacetraders.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The fuel status of a ship
 * @param current The current amount of fuel
 * @param capacity the ship's fuel capacity
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FuelStatus(int current, int capacity) {}
