package org.psu.spacetraders.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Additional information about the goods for sale at a marketplace
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeGood {

	private String symbol;
	private int tradeVolume;
	private int purchasePrice;
	private int sellPrice;

}