package org.psu.init;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.psu.miningmanager.MiningShipManager;
import org.psu.miningmanager.dto.MiningShipJob;
import org.psu.shiporchestrator.ShipJob;
import org.psu.shiporchestrator.ShipRole;
import org.psu.shiporchestrator.ShipRoleManager;
import org.psu.spacetraders.dto.Ship;
import org.psu.trademanager.TradeShipManager;
import org.psu.trademanager.dto.TradeShipJob;

/**
 * Tests for {@link ShipJobCreator}
 */
@ExtendWith(MockitoExtension.class)
public class ShipJobCreatorTest {

	@Mock
	private ShipRoleManager shipRoleManager;

	@Mock
	private MiningShipManager miningShipManager;

	@Mock
	private TradeShipManager tradeShipManager;

	@InjectMocks
	private ShipJobCreator shipJobCreator;

	/**
	 * Tests createShipJob with a mining ship
	 */
	@Test
	public void createShipJobMining() {

		final Ship ship = mock();
		when(shipRoleManager.determineRole(ship)).thenReturn(ShipRole.MINING);

		final MiningShipJob job = mock();
		when(miningShipManager.createJob(ship)).thenReturn(job);

		final ShipJob createdJob = shipJobCreator.createShipJob(ship);

		assertEquals(job, createdJob);
	}

	/**
	 * Tests createShipJob with a trade ship
	 */
	@Test
	public void createShipJobTrade() {

		final Ship ship = mock();
		when(shipRoleManager.determineRole(ship)).thenReturn(ShipRole.TRADE);

		final TradeShipJob job = mock();
		when(tradeShipManager.createJob(ship)).thenReturn(job);

		final ShipJob createdJob = shipJobCreator.createShipJob(ship);

		assertEquals(job, createdJob);
	}

	/**
	 * Tests createShipJob with a probe ship
	 */
	@Test
	public void createShipJobProbe() {

		final Ship ship = mock();
		when(shipRoleManager.determineRole(ship)).thenReturn(ShipRole.PROBE);

		final ShipJob createdJob = shipJobCreator.createShipJob(ship);

		assertNull(createdJob);
	}

}
