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
import org.psu.miningmanager.MiningShipManager;
import org.psu.miningmanager.MiningSiteManager;
import org.psu.miningmanager.dto.MiningShipJob;
import org.psu.navigation.RefuelPathCalculator;
import org.psu.shiporchestrator.ShipJobQueue;
import org.psu.shiporchestrator.ShipRole;
import org.psu.shiporchestrator.ShipRoleManager;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.api.ShipsClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.DataWrapper.WrapperMetadata;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipNavigation;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testutils.TestRequestThrottler;
import org.psu.trademanager.MarketplaceManager;
import org.psu.trademanager.TradeShipManager;
import org.psu.trademanager.dto.TradeShipJob;
import org.psu.websocket.WebsocketReporter;

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

		final MarketplaceManager marketplaceManager = mock();
		final ShipJobQueue jobQueue = mock();
		final MiningSiteManager miningSiteManager = mock();
		final RefuelPathCalculator pathCalculator = mock();
		final WebsocketReporter websocketReporter = mock();
		final RequestThrottler throttler = TestRequestThrottler.get();
		final ShipLoader shipLoader = new ShipLoader(limit, shipsClient, throttler, null, shipRoleManager,
				null, null, marketplaceManager, miningSiteManager, jobQueue, pathCalculator, websocketReporter);

		final List<Ship> ships = shipLoader.gatherShips();

		assertEquals(2, ships.size());
		assertTrue(ships.contains(ship1));
		assertTrue(ships.contains(ship2));

		verify(websocketReporter).updateShips(ships);
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

		final Ship tradeShip = mock(Ship.class);
		when(tradeShip.getNav()).thenReturn(shipNav);
		final Ship miningShip = mock(Ship.class);
		when(miningShip.getNav()).thenReturn(shipNav);
		final Ship probeShip = mock(Ship.class);
		when(probeShip.getNav()).thenReturn(shipNav);

		final WrapperMetadata metaData = new WrapperMetadata(1, 0, limit);
		final DataWrapper<List<Ship>> shipResponse1 = new DataWrapper<>(List.of(tradeShip, miningShip, probeShip),
				metaData);
		when(shipsClient.getShips(limit, 1)).thenReturn(shipResponse1);

		final ShipRoleManager shipRoleManager = mock(ShipRoleManager.class);
		when(shipRoleManager.determineRole(tradeShip)).thenReturn(ShipRole.TRADE);
		when(shipRoleManager.determineRole(miningShip)).thenReturn(ShipRole.MINING);
		when(shipRoleManager.determineRole(probeShip)).thenReturn(ShipRole.PROBE);

		final TradeShipJob tradeJob = mock(TradeShipJob.class);
		final TradeShipManager tradeShipManager = mock(TradeShipManager.class);
		when(tradeShipManager.createJob(tradeShip)).thenReturn(tradeJob);

		final MiningShipJob miningJob = mock(MiningShipJob.class);
		final MiningShipManager miningShipManager = mock(MiningShipManager.class);
		when(miningShipManager.createJob(miningShip)).thenReturn(miningJob);

		final MarketplaceManager marketplaceManager = mock(MarketplaceManager.class);

		final Waypoint waypoint = mock(Waypoint.class);
		final MarketInfo marketInfo = mock(MarketInfo.class);
		when(marketInfo.sellsProduct(Product.FUEL)).thenReturn(true);
		when(systemBuilder.gatherWaypoints(systemId)).thenReturn(List.of(waypoint));
    	when(systemBuilder.gatherMarketInfo(List.of(waypoint))).thenReturn(Map.of(waypoint, marketInfo));
		final ShipJobQueue jobQueue = mock(ShipJobQueue.class);

		final MiningSiteManager miningSiteManager = mock();
		final RefuelPathCalculator pathCalculator = mock();
		final WebsocketReporter websocketReporter = mock();

		final RequestThrottler throttler = TestRequestThrottler.get();
		final ShipLoader shipLoader = new ShipLoader(limit, shipsClient, throttler, systemBuilder, shipRoleManager,
				miningShipManager, tradeShipManager, marketplaceManager, miningSiteManager, jobQueue, pathCalculator,
				websocketReporter);

		shipLoader.run();

		verify(systemBuilder).gatherWaypoints(systemId);
		verify(marketplaceManager).updateMarketData(Map.of(waypoint, marketInfo));
		verify(jobQueue).establishJobs(List.of(tradeJob, miningJob));
		verify(jobQueue).beginJobQueue();
		verify(miningSiteManager).addSites(List.of(waypoint));
		verify(pathCalculator).loadRefuelWaypoints(List.of(waypoint));
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

		final ShipRoleManager shipRoleManager = mock();
		final MarketplaceManager marketplaceManager = mock();
		final ShipJobQueue jobQueue = mock();
		final MiningSiteManager miningSiteManager = mock();
		final RefuelPathCalculator pathCalculator = mock();
		final WebsocketReporter websocketReporter = mock();

		final RequestThrottler throttler = TestRequestThrottler.get();
		final ShipLoader shipLoader = new ShipLoader(limit, shipsClient, throttler, systemBuilder, shipRoleManager,
				null, null, marketplaceManager, miningSiteManager, jobQueue, pathCalculator, websocketReporter);

		assertThrows(IllegalStateException.class, () -> shipLoader.run());
	}

}
