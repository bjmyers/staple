package org.psu.navigation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.util.LinkedList;
import java.util.Queue;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.dto.Waypoint;

/**
 * Tests for {@link NavigationPath}
 */
public class NavigationPathTest {

	/**
	 * Tests the combine method
	 */
	@Test
	public void combine() {

		final double len1 = 5.67;
		final Waypoint way1 = mock();
		final Waypoint way2 = mock();
		final Queue<Waypoint> ways = new LinkedList<>();
		ways.add(way1);
		ways.add(way2);
		final NavigationPath path1 = new NavigationPath(len1, ways);

		final double len2 = 1.23;
		final Waypoint way3 = mock();
		final Queue<Waypoint> ways2 = new LinkedList<>();
		ways2.add(way3);
		final NavigationPath path2 = new NavigationPath(len2, ways2);

		final NavigationPath combinedPath = NavigationPath.combine(path1, path2);

		assertEquals(len1 + len2, combinedPath.getLength(), 1e-9);
		assertEquals(way1, combinedPath.getWaypoints().poll());
		assertEquals(way2, combinedPath.getWaypoints().poll());
		assertEquals(way3, combinedPath.getWaypoints().poll());
		assertNull(combinedPath.getWaypoints().poll());
	}

}
