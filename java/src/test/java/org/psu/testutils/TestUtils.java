package org.psu.testutils;

import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Miscellaneous test utilities
 */
public class TestUtils {

	/**
	 * @param <T> The type of items
	 * @param items The items
	 * @return A queue of the items, in the order they were input
	 */
	@SuppressWarnings("unchecked")
	public static <T> Deque<T> makeQueue(T... items) {
		return Stream.of(items).collect(Collectors.toCollection(LinkedList::new));
	}

}
