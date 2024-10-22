package org.psu.spacetraders.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
	private List<TradeGood> tradeGoods;

	public List<Product> getPotentialExports(final MarketInfo importingMarket) {
		return this.exports.stream().filter(product -> importingMarket.getImports().contains(product)).toList();
	}

	public boolean sellsProduct(final Product product) {
		return exchange.contains(product) || exports.contains(product) || imports.contains(product);
	}

	/**
	 * Finds the most profitable {@link TradeRequest}s at this market
	 *
	 * @param productsToBuy The products which can be bought
	 * @param capacity      The total number of items to buy
	 * @param totalCredits  the number of credits the user has, should not spend
	 *                      more than half of their total
	 * @param knownRoute    If the route is known, if not, just buy one of every
	 *                      item
	 * @return The {@link TradeRequest}s
	 */
	public List<TradeRequest> buildPurchaseRequest(final List<Product> productsToBuy, final int capacity,
			final int totalCredits, final boolean knownRoute) {
		final Set<String> productSymbolsToBuy = productsToBuy.stream().map(Product::getSymbol)
				.collect(Collectors.toSet());
		final List<TradeGood> sortedTradeGoods = this.tradeGoods.stream()
				.filter(t -> productSymbolsToBuy.contains(t.getSymbol()))
				// Sort by purchase price, negate so that the most expensive item is first
				.sorted((t1, t2) -> -1 * Integer.compare(t1.getPurchasePrice(), t2.getPurchasePrice())).toList();

		final List<TradeRequest> output = new ArrayList<>();
		int remainingItemsToBuy = capacity;
		int remainingBudget = totalCredits / 2;

		for (final TradeGood tradeGood : sortedTradeGoods) {
			// Find how many items we can hold (in the ship or in the request)
			// If the route is not known, just try to buy one item
			final int requestCapacity = knownRoute ? Math.min(remainingItemsToBuy, tradeGood.getTradeVolume()) : 1;
			// Limit the quantity to buy if it would be too expensive
			final int quantityToBuy = Math.min(requestCapacity, remainingBudget / tradeGood.getPurchasePrice());
			output.add(new TradeRequest(tradeGood.getSymbol(), quantityToBuy));

			remainingItemsToBuy -= quantityToBuy;
			remainingBudget -= quantityToBuy * tradeGood.getPurchasePrice();
			if (remainingItemsToBuy <= 0 || remainingBudget <= 0) {
				// Unable to fit more items or unable to afford more items
				break;
			}
		}
		return output;
	}

	/**
	 * @param cargoItems the {@link CargoItem}s which are in the ship's cargo that
	 *                   are to be sold
	 * @return A list of {@link TradeRequests for the same products in the same
	 *         quantity that respects this market's trade limits
	 */
	public List<TradeRequest> buildSellRequests(final List<CargoItem> cargoItems) {
		final List<TradeRequest> output = new ArrayList<>();
		for (CargoItem item : cargoItems) {

			final Optional<TradeGood> optionalTradeGood = this.tradeGoods.stream()
					.filter(t -> item.symbol().equals(t.getSymbol())).findFirst();
			if (optionalTradeGood.isEmpty()) {
				// Item in cargo isn't traded here, skip
				continue;
			}
			final TradeGood tradeGood = optionalTradeGood.get();

			// Floor division, the number of full requests to make
			final int numFullRequestsToMake = item.units() / tradeGood.getTradeVolume();
			for (int i = 0; i < numFullRequestsToMake; i++) {
				output.add(new TradeRequest(item.symbol(), tradeGood.getTradeVolume()));
			}

			// The remaining goods which couldn't fit into a full trade request
			final int remainingGoods = item.units() % tradeGood.getTradeVolume();
			if (remainingGoods > 0) {
				output.add(new TradeRequest(item.symbol(), remainingGoods));
			}
		}
		return output;
	}

}
