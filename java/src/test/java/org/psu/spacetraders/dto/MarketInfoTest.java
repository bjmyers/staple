package org.psu.spacetraders.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests for {@link MarketInfo}
 */
public class MarketInfoTest {

	/**
	 * Tests if market info can be deserialized
	 * @throws IOException
	 * @throws DatabindException
	 * @throws StreamReadException
	 */
	@Test
	public void deserialize() throws StreamReadException, DatabindException, IOException {

		final File jsonFile = new File("src/test/resources/marketInfo.json");
		final ObjectMapper mapper = new ObjectMapper();

		final MarketInfo marketInfo = mapper.readValue(jsonFile, MarketInfo.class);

		assertEquals(5, marketInfo.getImports().size());
		assertTrue(marketInfo.getImports().contains(new Product("ALUMINUM")));
		assertTrue(marketInfo.getImports().contains(new Product("PLASTICS")));
		assertTrue(marketInfo.getImports().contains(new Product("FABRICS")));
		assertTrue(marketInfo.getImports().contains(new Product("POLYNUCLEOTIDES")));
		assertTrue(marketInfo.getImports().contains(new Product("FERTILIZERS")));

		assertEquals(4, marketInfo.getExports().size());
		assertTrue(marketInfo.getExports().contains(new Product("FOOD")));
		assertTrue(marketInfo.getExports().contains(new Product("CLOTHING")));
		assertTrue(marketInfo.getExports().contains(new Product("BIOCOMPOSITES")));
		assertTrue(marketInfo.getExports().contains(new Product("EQUIPMENT")));

		assertEquals(1, marketInfo.getExchange().size());
		assertTrue(marketInfo.getExchange().contains(new Product("FUEL")));
	}

	/**
	 * Tests {@link MarketInfo#getPotentialExports}
	 */
	@Test
	public void getPotentialExports() {

		final Product p1 = new Product("A");
		final Product p2 = new Product("B");
		final Product p3 = new Product("C");
		final Product p4 = new Product("D");

		// The only common product exported by the exporting market and imported by the importing market is p1
		final MarketInfo exportingMarket = new MarketInfo(null, List.of(), List.of(p1, p2, p3), List.of(), null);
		final MarketInfo importingMarket = new MarketInfo(null, List.of(p1, p4), List.of(p2), List.of(p3), null);

		final List<Product> potentialExports = exportingMarket.getPotentialExports(importingMarket);

		assertEquals(1, potentialExports.size());
		assertTrue(potentialExports.contains(p1));
	}

	/**
	 * Tests {@link MarketInfo#sellsProduct}
	 */
	@Test
	public void sellsProduct() {
		final Product product = new Product("A");

		final MarketInfo importingMarket = new MarketInfo(null, List.of(product), List.of(), List.of(), null);
		final MarketInfo exportingMarket = new MarketInfo(null, List.of(), List.of(product), List.of(), null);
		final MarketInfo exchangingMarket = new MarketInfo(null, List.of(), List.of(), List.of(product), null);
		final MarketInfo notSellingMarket = new MarketInfo(null, List.of(), List.of(), List.of(), null);

		assertTrue(importingMarket.sellsProduct(product));
		assertTrue(exportingMarket.sellsProduct(product));
		assertTrue(exchangingMarket.sellsProduct(product));
		assertFalse(notSellingMarket.sellsProduct(product));
	}

	/**
	 * Tests {@link MarketInfo#buildPurchaseRequest}
	 */
	@Test
	public void buildPurchaseRequest() {

		final String productSymbol1 = "milk";
		final String productSymbol2 = "eggs";
		final String productSymbol3 = "cheese";
		final String productSymbol4 = "bread";

		final Product product1 = new Product(productSymbol1);
		final Product product2 = new Product(productSymbol2);
		final Product product4 = new Product(productSymbol4);

		final TradeGood tradeGood1 = new TradeGood();
		tradeGood1.setPurchasePrice(100);
		tradeGood1.setSymbol(productSymbol1);
		tradeGood1.setTradeVolume(10);

		final TradeGood tradeGood2 = new TradeGood();
		tradeGood2.setPurchasePrice(200);
		tradeGood2.setSymbol(productSymbol2);
		tradeGood2.setTradeVolume(5);

		final TradeGood tradeGood3 = new TradeGood();
		tradeGood3.setPurchasePrice(50);
		tradeGood3.setSymbol(productSymbol3);
		tradeGood3.setTradeVolume(15);

		final TradeGood tradeGood4 = new TradeGood();
		tradeGood4.setPurchasePrice(500);
		tradeGood4.setSymbol(productSymbol4);
		tradeGood4.setTradeVolume(1);

		final MarketInfo marketInfo = new MarketInfo(null, null, null, null,
				List.of(tradeGood1, tradeGood2, tradeGood3, tradeGood4));

		// Want to purchase products 1, 2, and 4
		// Sorted by most expensive to least expensive: 4, 2, 1
		// With 4 items to buy, we can purchase 1 unit of product4, 3 units of product2,
		// and 0 units of product1

		final List<TradeRequest> tradeRequests = marketInfo.buildPurchaseRequest(
				List.of(product1, product2, product4), 4, 10000, true);

		assertEquals(2, tradeRequests.size());

		final Map<String, TradeRequest> requestsByProductSymbol = tradeRequests.stream()
				.collect(Collectors.toMap(TradeRequest::getSymbol, Function.identity()));

		assertEquals(1, requestsByProductSymbol.get(productSymbol4).getUnits());
		assertEquals(3, requestsByProductSymbol.get(productSymbol2).getUnits());
	}

	/**
	 * Tests {@link MarketInfo#buildPurchaseRequest} when the requests hit the max spend limit
	 */
	@Test
	public void buildPurchaseRequestInsufficientFunds() {

		final String productSymbol1 = "milk";
		final String productSymbol2 = "eggs";
		final String productSymbol3 = "cheese";

		final Product product1 = new Product(productSymbol1);
		final Product product2 = new Product(productSymbol2);
		final Product product3 = new Product(productSymbol3);

		final TradeGood tradeGood1 = new TradeGood();
		tradeGood1.setPurchasePrice(200);
		tradeGood1.setSymbol(productSymbol1);
		tradeGood1.setTradeVolume(5);

		final TradeGood tradeGood2 = new TradeGood();
		tradeGood2.setPurchasePrice(100);
		tradeGood2.setSymbol(productSymbol2);
		tradeGood2.setTradeVolume(10);

		final TradeGood tradeGood3 = new TradeGood();
		tradeGood3.setPurchasePrice(50);
		tradeGood3.setSymbol(productSymbol3);
		tradeGood3.setTradeVolume(15);

		final MarketInfo marketInfo = new MarketInfo(null, null, null, null,
				List.of(tradeGood1, tradeGood2, tradeGood3));

		// We have 3000 credits, so we don't want to spend more than 1500 on this request
		// We will buy all 5 tradeGood1's, spending 1000 credits
		// We can buy 5 tradeGood2's, spending 500 additional credits

		final List<TradeRequest> tradeRequests = marketInfo.buildPurchaseRequest(
				List.of(product1, product2, product3), 100, 3000, true);

		assertEquals(2, tradeRequests.size());

		final Map<String, TradeRequest> requestsByProductSymbol = tradeRequests.stream()
				.collect(Collectors.toMap(TradeRequest::getSymbol, Function.identity()));

		assertEquals(5, requestsByProductSymbol.get(productSymbol1).getUnits());
		assertEquals(5, requestsByProductSymbol.get(productSymbol2).getUnits());
	}

	/**
	 * Tests {@link MarketInfo#buildPurchaseRequest} with an unknown route
	 */
	@Test
	public void buildPurchaseRequestUnknownRoute() {

		final String productSymbol1 = "milk";
		final String productSymbol2 = "eggs";
		final String productSymbol3 = "cheese";
		final String productSymbol4 = "bread";

		final Product product1 = new Product(productSymbol1);
		final Product product2 = new Product(productSymbol2);
		final Product product4 = new Product(productSymbol4);

		final TradeGood tradeGood1 = new TradeGood();
		tradeGood1.setPurchasePrice(100);
		tradeGood1.setSymbol(productSymbol1);
		tradeGood1.setTradeVolume(10);

		final TradeGood tradeGood2 = new TradeGood();
		tradeGood2.setPurchasePrice(200);
		tradeGood2.setSymbol(productSymbol2);
		tradeGood2.setTradeVolume(5);

		final TradeGood tradeGood3 = new TradeGood();
		tradeGood3.setPurchasePrice(50);
		tradeGood3.setSymbol(productSymbol3);
		tradeGood3.setTradeVolume(15);

		final TradeGood tradeGood4 = new TradeGood();
		tradeGood4.setPurchasePrice(500);
		tradeGood4.setSymbol(productSymbol4);
		tradeGood4.setTradeVolume(1);

		final MarketInfo marketInfo = new MarketInfo(null, null, null, null,
				List.of(tradeGood1, tradeGood2, tradeGood3, tradeGood4));

		// We want to buy one of each item because the route is unknown, but have a capacity of 3
		final List<TradeRequest> tradeRequests = marketInfo.buildPurchaseRequest(
				List.of(product1, product2, product4), 3, 10000, false);

		assertEquals(3, tradeRequests.size());

		final Map<String, TradeRequest> requestsByProductSymbol = tradeRequests.stream()
				.collect(Collectors.toMap(TradeRequest::getSymbol, Function.identity()));

		assertEquals(1, requestsByProductSymbol.get(productSymbol4).getUnits());
		assertEquals(1, requestsByProductSymbol.get(productSymbol2).getUnits());
		assertEquals(1, requestsByProductSymbol.get(productSymbol1).getUnits());
	}

	/**
	 * Tests {@link MarketInfo#buildSellRequests}
	 */
	@Test
	public void buildSellRequests() {

		// This product starts as a single request and will end as a full request
		final String product1 = "p1";
		final TradeGood tradeGood1 = new TradeGood();
		tradeGood1.setSymbol(product1);
		tradeGood1.setTradeVolume(10);
		final CargoItem cargoItem1 = new CargoItem(product1, 10);

		// This product starts as a single request and will not quite fill up a full request
		final String product2 = "p2";
		final TradeGood tradeGood2 = new TradeGood();
		tradeGood2.setSymbol(product2);
		tradeGood2.setTradeVolume(10);
		final CargoItem cargoItem2 = new CargoItem(product2, 8);

		// This product starts as a single request and will turn into 3 requests
		final String product3 = "p3";
		final TradeGood tradeGood3 = new TradeGood();
		tradeGood3.setSymbol(product3);
		tradeGood3.setTradeVolume(8);
		final CargoItem cargoItem3 = new CargoItem(product3, 20);

		final MarketInfo marketInfo = new MarketInfo();
		marketInfo.setTradeGoods(List.of(tradeGood1, tradeGood2, tradeGood3));

		final List<TradeRequest> sellRequests = marketInfo.buildSellRequests(
				List.of(cargoItem1, cargoItem2, cargoItem3));

		assertEquals(5, sellRequests.size());

		final Map<String, List<TradeRequest>> requestsByProduct = sellRequests.stream()
				.collect(Collectors.groupingBy(TradeRequest::getSymbol));

		final List<TradeRequest> product1Requests = requestsByProduct.get(product1);
		assertEquals(1, product1Requests.size());
		assertEquals(10, product1Requests.get(0).getUnits());

		final List<TradeRequest> product2Requests = requestsByProduct.get(product2);
		assertEquals(1, product2Requests.size());
		assertEquals(8, product2Requests.get(0).getUnits());

		final List<TradeRequest> product3Requests = requestsByProduct.get(product3);
		assertEquals(3, product3Requests.size());
		//TODO: Get assertJ working so that I don't have to do this nonsense
		assertEquals(8, product3Requests.get(0).getUnits());
		assertEquals(8, product3Requests.get(1).getUnits());
		assertEquals(4, product3Requests.get(2).getUnits());

	}

}
