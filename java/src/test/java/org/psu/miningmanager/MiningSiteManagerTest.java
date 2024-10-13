package org.psu.miningmanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.Trait;
import org.psu.spacetraders.dto.Trait.Type;
import org.psu.spacetraders.dto.Waypoint;

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

		final Ship ship = mock(Ship.class);

		final Waypoint closeMiningSite = mock(Waypoint.class);
		when(closeMiningSite.getSymbol()).thenReturn("closeMiningSite");
		when(closeMiningSite.getTraits()).thenReturn(List.of(miningTrait, nonMiningTrait));
		when(ship.distTo(closeMiningSite)).thenReturn(1.0);

		final Waypoint mediumMiningSite = mock(Waypoint.class);
		when(mediumMiningSite.getSymbol()).thenReturn("mediumMiningSite");
		when(mediumMiningSite.getTraits()).thenReturn(List.of(miningTrait));
		when(ship.distTo(mediumMiningSite)).thenReturn(2.0);

		final Waypoint farMiningSite = mock(Waypoint.class);
		when(farMiningSite.getSymbol()).thenReturn("farMiningSite");
		when(farMiningSite.getTraits()).thenReturn(List.of(miningTrait, nonMiningTrait));
		when(ship.distTo(farMiningSite)).thenReturn(3.0);

		final Waypoint nonMiningSite = mock(Waypoint.class);
		when(nonMiningSite.getSymbol()).thenReturn("nonMiningSite");
		when(nonMiningSite.getTraits()).thenReturn(List.of(nonMiningTrait));
		when(ship.distTo(nonMiningSite)).thenReturn(0.1);

		final MiningSiteManager manager = new MiningSiteManager();
		manager.addSites(List.of(nonMiningSite, closeMiningSite, mediumMiningSite, farMiningSite));

		final Optional<Waypoint> closestMiningSite = manager.getClosestMiningSite(ship);
		assertTrue(closestMiningSite.isPresent());
		assertEquals(closeMiningSite, closestMiningSite.get());
	}

}
