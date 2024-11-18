package org.psu.testdriver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.dto.Waypoint;

/**
 * Tests for {@link LocalWaypointsManager}
 */
public class LocalWaypointsManagerTest {

	/**
	 * Tests getWaypoints
	 */
	@Test
	public void getWaypoints() {
		final LocalWaypointsManager waypointsManager = new LocalWaypointsManager();

		final List<Waypoint> waypoints = waypointsManager.getWaypoints();
		assertEquals(6, waypoints.size());

		// Do it again for lazy loading
		final List<Waypoint> waypoints2 = waypointsManager.getWaypoints();
		assertEquals(6, waypoints2.size());
	}
}
