package org.psu.navigation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

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
		final NavigationPath path1 = new NavigationPath(len1, List.of(way1, way2));

		final double len2 = 1.23;
		final Waypoint way3 = mock();
		final NavigationPath path2 = new NavigationPath(len2, List.of(way3));

		final NavigationPath combinedPath = NavigationPath.combine(path1, path2);

		assertEquals(len1 + len2, combinedPath.getLength(), 1e-9);
		assertEquals(List.of(way1, way2, way3), combinedPath.getWaypoints());
	}

}
