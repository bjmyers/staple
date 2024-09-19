package org.psu.spacetraders.api;

import org.psu.spacetraders.dto.DataWrapper.WrapperMetadata;

/**
 * Miscellaneous utils for the Space Traders API
 */
public class SpaceTradersUtils {

	/**
	 * @param metaData the metadata from the first call to a paged SpaceTraders API
	 * @return The total number of pages needed to gather everything from this
	 *         resource, including the first call which was used to get the
	 *         metaData, assuming the same limit is used in each call
	 */
	public static int getTotalNumPages(final WrapperMetadata metaData) {
		if (metaData.getTotal() % metaData.getLimit() == 0) {
			return metaData.getTotal() / metaData.getLimit();
		}
		return metaData.getTotal() / metaData.getLimit() + 1;
	}

}
