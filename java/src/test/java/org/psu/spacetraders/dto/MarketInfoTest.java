package org.psu.spacetraders.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

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
}
