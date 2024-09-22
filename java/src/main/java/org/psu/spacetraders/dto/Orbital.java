package org.psu.spacetraders.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Designates a point where a ship can orbit
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Orbital {

	private String symbol;
}
