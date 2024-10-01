package org.psu.spacetraders.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Encapsulates the products that a marketplace trades
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketInfo {

	private List<Product> imports;
	private List<Product> exports;
	private List<Product> exchange;

	public List<Product> getPotentialExports(final MarketInfo importingMarket) {
		return this.exports.stream().filter(product -> importingMarket.getImports().contains(product)).toList();
	}

}
