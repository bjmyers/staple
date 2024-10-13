package org.psu.miningmanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.psu.miningmanager.dto.MiningShipJob;
import org.psu.miningmanager.dto.MiningShipJob.State;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.Waypoint;


/**
 * Tests for {@link MiningShipManager}
 */
public class MiningShipManagerTest {

	/**
	 * Tests createJob
	 */
	@Test
	public void createJob() {

		final Ship ship = mock(Ship.class);

		final Waypoint way = mock(Waypoint.class);
		final MiningSiteManager miningSiteManager = mock(MiningSiteManager.class);
		when(miningSiteManager.getClosestMiningSite(ship)).thenReturn(Optional.of(way));

		final MiningShipManager manager = new MiningShipManager(miningSiteManager);

		final MiningShipJob job = manager.createJob(ship);

		assertEquals(ship, job.getShip());
		assertEquals(way, job.getExtractionPoint());
		assertEquals(State.TRAVELING_TO_RESOURCE, job.getState());
	}

	/**
	 * Tests manageMiningShip
	 */
	@Test
	public void manageMiningShip() {

		final MiningShipJob job = mock(MiningShipJob.class);

		final MiningShipManager manager = new MiningShipManager(null);

		final MiningShipJob nextJob = manager.manageMiningShip(job);

		assertEquals(job, nextJob);
	}

}
