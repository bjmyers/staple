package org.psu.spacetraders.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * A request to trade with a market
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class TradeRequest {

	private String symbol;
	private int units;
}
