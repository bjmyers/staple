package org.psu.spacetraders.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
		final MarketInfo exportingMarket = new MarketInfo(List.of(), List.of(p1, p2, p3), List.of());
		final MarketInfo importingMarket = new MarketInfo(List.of(p1, p4), List.of(p2), List.of(p3));

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

		final MarketInfo importingMarket = new MarketInfo(List.of(product), List.of(), List.of());
		final MarketInfo exportingMarket = new MarketInfo(List.of(), List.of(product), List.of());
		final MarketInfo exchangingMarket = new MarketInfo(List.of(), List.of(), List.of(product));
		final MarketInfo notSellingMarket = new MarketInfo(List.of(), List.of(), List.of());

		assertTrue(importingMarket.sellsProduct(product));
		assertTrue(exportingMarket.sellsProduct(product));
		assertTrue(exchangingMarket.sellsProduct(product));
		assertFalse(notSellingMarket.sellsProduct(product));
	}

}
