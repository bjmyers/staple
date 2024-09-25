package org.psu.init;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.api.WaypointsClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.DataWrapper.WrapperMetadata;
import org.psu.spacetraders.dto.Waypoint;

/**
 * Tests for {@link SystemBuilder}
 */
public class SystemBuilderTest {

	/**
	 * Tests {@link SystemBuilder#getWaypoints}
	 */
	@Test
	public void getWaypoints() {

		// Data will come in two pages
		final int total = 12;
		final int limit = 10;
		final WrapperMetadata metaData = new WrapperMetadata(total, 0, limit);

		final List<Waypoint> waypointPage1 = List.of(mock(Waypoint.class), mock(Waypoint.class));
		final List<Waypoint> waypointPage2 = List.of(mock(Waypoint.class));
		final DataWrapper<List<Waypoint>> waypointResponse1 = new DataWrapper<>(waypointPage1, metaData);
		final DataWrapper<List<Waypoint>> waypointResponse2 = new DataWrapper<>(waypointPage2, metaData);

		final String systemId = "I'm a system";
		final WaypointsClient waypointsClient = mock(WaypointsClient.class);
		when(waypointsClient.getWaypoints(systemId, limit, 1)).thenReturn(waypointResponse1);
		when(waypointsClient.getWaypoints(systemId, limit, 2)).thenReturn(waypointResponse2);

		final SystemBuilder builder = new SystemBuilder(limit, waypointsClient);

		final List<Waypoint> waypoints = builder.gatherWaypoints(systemId);

		assertEquals(3, waypoints.size());
		assertTrue(waypoints.containsAll(waypointPage1));
		assertTrue(waypoints.containsAll(waypointPage2));
	}

}
