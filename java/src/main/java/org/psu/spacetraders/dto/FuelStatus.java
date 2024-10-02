package org.psu.spacetraders.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The current fuel status of a ship
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FuelStatus(int current, int capacity) {}
