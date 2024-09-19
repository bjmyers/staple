package org.psu.spacetraders.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.dto.DataWrapper.WrapperMetadata;

/**
 * Tests for {@link SpaceTradersUtils}
 */
public class SpaceTradersUtilsTest {

	/**
	 * Tests {@link SpaceTradersUtils#getTotalNumPages} when a single page is enough
	 * to gather all of the data
	 */
	@Test
	public void getTotalNumPagesSinglePage() {
		final int total = 12;
		final int page = 1;
		final int limit = 20;
		final WrapperMetadata metadata = new WrapperMetadata(total, page, limit);

		final int numPages = SpaceTradersUtils.getTotalNumPages(metadata);

		assertEquals(1, numPages);
	}

	/**
	 * Tests {@link SpaceTradersUtils#getTotalNumPages} when multiple pages are required
	 * to gather all of the data
	 */
	@Test
	public void getTotalNumPagesMultiplePages() {
		final int total = 55;
		final int page = 1;
		final int limit = 10;
		final WrapperMetadata metadata = new WrapperMetadata(total, page, limit);

		final int numPages = SpaceTradersUtils.getTotalNumPages(metadata);

		assertEquals(6, numPages);
	}

	/**
	 * Tests {@link SpaceTradersUtils#getTotalNumPages} when the total number of
	 * items is an exact multiple of the limit
	 */
	@Test
	public void getTotalNumPagesExactMultiple() {
		final int total = 50;
		final int page = 1;
		final int limit = 10;
		final WrapperMetadata metadata = new WrapperMetadata(total, page, limit);

		final int numPages = SpaceTradersUtils.getTotalNumPages(metadata);

		assertEquals(5, numPages);
	}

	/**
	 * Tests {@link SpaceTradersUtils#getTotalNumPages} when the number of items
	 * exactly equals the limit
	 */
	@Test
	public void getTotalNumPagesExactSinglePage() {
		final int total = 8;
		final int page = 1;
		final int limit = 8;
		final WrapperMetadata metadata = new WrapperMetadata(total, page, limit);

		final int numPages = SpaceTradersUtils.getTotalNumPages(metadata);

		assertEquals(1, numPages);
	}

}
