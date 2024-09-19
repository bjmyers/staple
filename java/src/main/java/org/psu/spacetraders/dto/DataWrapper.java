package org.psu.spacetraders.dto;

import lombok.Data;

/**
 * Wrapper for SpaceTraders responses
 */
@Data
public class DataWrapper<T> {

	private final T data;
	private final WrapperMetadata meta;

	/**
	 * The metadata on a wrapped SpaceTraders response
	 */
	@Data
	public static class WrapperMetadata {

		private final int total;
		private final int page;
		private final int limit;
	}

}