package org.psu.init;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.psu.shiporchestrator.ShipJobQueue;
import org.psu.shiporchestrator.ShipRole;
import org.psu.shiporchestrator.ShipRoleManager;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.api.ShipsClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.DataWrapper.WrapperMetadata;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipNavigation;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testutils.TestRequestThrottler;
import org.psu.trademanager.MarketplaceManager;
import org.psu.trademanager.TradeShipManager;
import org.psu.trademanager.dto.TradeShipJob;

/**
 * Tests for {@link ShipLoader}
 */
public class ShipLoaderTest {

	/**
	 * Tests {@link ShipLoader#gatherShips}
	 */
	@Test
	public void gatherShips() {

		// Data will come in two pages
		final int total = 12;
		final int limit = 10;
		final WrapperMetadata metaData = new WrapperMetadata(total, 0, limit);

		final Ship ship1 = mock(Ship.class);
		final Ship ship2 = mock(Ship.class);
		final DataWrapper<List<Ship>> shipResponse1 = new DataWrapper<>(List.of(ship1), metaData);
		final DataWrapper<List<Ship>> shipResponse2 = new DataWrapper<>(List.of(ship2), metaData);

		final ShipsClient shipsClient = mock(ShipsClient.class);
		when(shipsClient.getShips(limit, 1)).thenReturn(shipResponse1);
		when(shipsClient.getShips(limit, 2)).thenReturn(shipResponse2);
		final ShipRoleManager shipRoleManager = mock(ShipRoleManager.class);
		final TradeShipManager tradeShipManager = mock(TradeShipManager.class);

		final MarketplaceManager marketplaceManager = mock(MarketplaceManager.class);
		final ShipJobQueue jobQueue = mock(ShipJobQueue.class);
		final RequestThrottler throttler = TestRequestThrottler.get();
		final ShipLoader shipLoader = new ShipLoader(limit, shipsClient, throttler, null, shipRoleManager,
				tradeShipManager, marketplaceManager, jobQueue);

		final List<Ship> ships = shipLoader.gatherShips();

		assertEquals(2, ships.size());
		assertTrue(ships.contains(ship1));
		assertTrue(ships.contains(ship2));
	}

	/**
	 * Tests {@link ShipLoader#run}
	 */
	@Test
	public void run() {
		final int limit = 20;
		final ShipsClient shipsClient = mock(ShipsClient.class);
		final SystemBuilder systemBuilder = mock(SystemBuilder.class);

		final String systemId = "I'm a system";
		final ShipNavigation shipNav = mock(ShipNavigation.class);
		when(shipNav.getSystemSymbol()).thenReturn(systemId);

		final Ship ship = mock(Ship.class);
		when(ship.getNav()).thenReturn(shipNav);

		final WrapperMetadata metaData = new WrapperMetadata(1, 0, limit);
		final DataWrapper<List<Ship>> shipResponse1 = new DataWrapper<>(List.of(ship), metaData);
		when(shipsClient.getShips(limit, 1)).thenReturn(shipResponse1);

		final ShipRoleManager shipRoleManager = mock(ShipRoleManager.class);
		when(shipRoleManager.determineRole(ship)).thenReturn(ShipRole.MINING);

		final TradeShipJob job = mock(TradeShipJob.class);
		final TradeShipManager tradeShipManager = mock(TradeShipManager.class);
		when(tradeShipManager.createJob(ship)).thenReturn(job);
		final MarketplaceManager marketplaceManager = mock(MarketplaceManager.class);

		final Waypoint waypoint = mock(Waypoint.class);
		final MarketInfo marketInfo = mock(MarketInfo.class);
		when(systemBuilder.gatherWaypoints(systemId)).thenReturn(List.of(waypoint));
    	when(systemBuilder.gatherMarketInfo(List.of(waypoint))).thenReturn(Map.of(waypoint, marketInfo));
		final ShipJobQueue jobQueue = mock(ShipJobQueue.class);

		final RequestThrottler throttler = TestRequestThrottler.get();
		final ShipLoader shipLoader = new ShipLoader(limit, shipsClient, throttler, systemBuilder, shipRoleManager,
				tradeShipManager, marketplaceManager, jobQueue);

		shipLoader.run();

		verify(systemBuilder).gatherWaypoints(systemId);
		verify(marketplaceManager).updateMarketData(Map.of(waypoint, marketInfo));
		verify(jobQueue).establishJobs(List.of(job));
		verify(jobQueue).beginJobQueue();
	}

	/**
	 * Tests {@link ShipLoader#onStartup} when the user has no ships
	 */
	@Test
	public void onStartupNoShips() {
		final int limit = 20;
		final ShipsClient shipsClient = mock(ShipsClient.class);
		final SystemBuilder systemBuilder = mock(SystemBuilder.class);

		final WrapperMetadata metaData = new WrapperMetadata(1, 0, limit);
		// No Ships!
		final DataWrapper<List<Ship>> shipResponse1 = new DataWrapper<>(List.of(), metaData);
		when(shipsClient.getShips(limit, 1)).thenReturn(shipResponse1);

		final ShipRoleManager shipRoleManager = mock(ShipRoleManager.class);
		final TradeShipManager tradeShipManager = mock(TradeShipManager.class);
		final MarketplaceManager marketplaceManager = mock(MarketplaceManager.class);
		final ShipJobQueue jobQueue = mock(ShipJobQueue.class);

		final RequestThrottler throttler = TestRequestThrottler.get();
		final ShipLoader shipLoader = new ShipLoader(limit, shipsClient, throttler, systemBuilder, shipRoleManager,
				tradeShipManager, marketplaceManager, jobQueue);

		assertThrows(IllegalStateException.class, () -> shipLoader.run());
	}

}
