package org.psu.spacetraders.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * The response from a request to refuel
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefuelResponse {

	private Agent agent;
	private FuelStatus fuel;
	private Transaction transaction;

}
