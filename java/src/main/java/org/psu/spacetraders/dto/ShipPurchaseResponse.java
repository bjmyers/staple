package org.psu.spacetraders.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * A response from purchasing a ship
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipPurchaseResponse {

	private Agent agent;
	private Ship ship;
	private Transaction transaction;

}
