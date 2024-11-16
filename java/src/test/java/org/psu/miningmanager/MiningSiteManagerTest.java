package org.psu.miningmanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import org.junit.jupiter.api.Test;
import org.psu.navigation.NavigationPath;
import org.psu.navigation.RefuelPathCalculator;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.Trait;
import org.psu.spacetraders.dto.Trait.Type;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testutils.TestUtils;

/**
 * Tests for {@link MiningSiteManager}
 */
public class MiningSiteManagerTest {

	/**
	 * Tests getClosestMiningSite
	 */
	@Test
	public void getClosestMiningSite() {

		final Trait miningTrait = new Trait(Type.COMMON_METAL_DEPOSITS);
		final Trait nonMiningTrait = new Trait(Type.DEEP_CRATERS);

		final RefuelPathCalculator pathCalculator = mock();
		final Ship ship = mock();

		final String closeMiningSiteId = "closeMiningSite";
		final Waypoint closeMiningSite = mock(Waypoint.class);
		when(closeMiningSite.getSymbol()).thenReturn(closeMiningSiteId);
		when(closeMiningSite.getTraits()).thenReturn(List.of(miningTrait, nonMiningTrait));
		when(pathCalculator.determineShortestRoute(ship, closeMiningSite))
				.thenReturn(new NavigationPath(1.0, TestUtils.makeQueue(closeMiningSite)));

		final String mediumMiningSiteId = "mediumMiningSite";
		final Waypoint mediumMiningSite = mock(Waypoint.class);
		when(mediumMiningSite.getSymbol()).thenReturn(mediumMiningSiteId);
		when(mediumMiningSite.getTraits()).thenReturn(List.of(miningTrait));
		when(pathCalculator.determineShortestRoute(ship, mediumMiningSite))
				.thenReturn(new NavigationPath(2.0, TestUtils.makeQueue(mediumMiningSite)));

		final String farMiningSiteId = "farMiningSite";
		final Waypoint farMiningSite = mock(Waypoint.class);
		when(farMiningSite.getSymbol()).thenReturn(farMiningSiteId);
		when(farMiningSite.getTraits()).thenReturn(List.of(miningTrait, nonMiningTrait));
		when(pathCalculator.determineShortestRoute(ship, farMiningSite))
				.thenReturn(new NavigationPath(2.0, TestUtils.makeQueue(farMiningSite)));

		final String nonMiningSiteId = "nonMiningSite";
		final Waypoint nonMiningSite = mock(Waypoint.class);
		when(nonMiningSite.getSymbol()).thenReturn(nonMiningSiteId);
		when(nonMiningSite.getTraits()).thenReturn(List.of(nonMiningTrait));
		when(ship.distTo(nonMiningSite)).thenReturn(0.1);

		final MiningSiteManager manager = new MiningSiteManager(pathCalculator);
		manager.addSites(List.of(nonMiningSite, closeMiningSite, mediumMiningSite, farMiningSite));

		final Optional<Deque<Waypoint>> closestMiningSite = manager.getClosestMiningSite(ship);
		assertTrue(closestMiningSite.isPresent());
		final Queue<Waypoint> path = closestMiningSite.get();
		assertEquals(closeMiningSite, path.poll());
		assertNull(path.poll());

		assertEquals(closeMiningSite, manager.getMiningSite(closeMiningSiteId));
		assertEquals(mediumMiningSite, manager.getMiningSite(mediumMiningSiteId));
		assertEquals(farMiningSite, manager.getMiningSite(farMiningSiteId));
		assertNull(manager.getMiningSite(nonMiningSiteId));
	}

}
