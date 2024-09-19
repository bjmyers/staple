package org.psu.init;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
	 * Tests {@link SystemBuilder#onStartup}
	 */
	@Test
	public void onStartup() {

		// Data will come in two pages
		final int total = 12;
		final int limit = 10;
		final WrapperMetadata metaData = new WrapperMetadata(total, 0, limit);
		final DataWrapper<List<Waypoint>> waypointResponse = new DataWrapper<>(List.of(), metaData);

		final WaypointsClient waypointsClient = mock(WaypointsClient.class);
		when(waypointsClient.getWaypoints(any(), anyInt(), anyInt())).thenReturn(waypointResponse);

		final String systemId = "I'm a system";
		final SystemBuilder builder = new SystemBuilder(systemId, limit, waypointsClient);

		builder.onStartup(null);

		verify(waypointsClient).getWaypoints(systemId, limit, 1);
		verify(waypointsClient).getWaypoints(systemId, limit, 2);
		verifyNoMoreInteractions(waypointsClient);

	}

}
