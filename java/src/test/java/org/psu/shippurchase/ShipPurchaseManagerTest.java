package org.psu.shippurchase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.psu.init.ShipJobCreator;
import org.psu.navigation.NavigationPath;
import org.psu.navigation.RefuelPathCalculator;
import org.psu.shiporchestrator.ShipJob;
import org.psu.spacetraders.api.MarketplaceRequester;
import org.psu.spacetraders.api.NavigationHelper;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipNavigation;
import org.psu.spacetraders.dto.ShipPurchaseRequest;
import org.psu.spacetraders.dto.ShipPurchaseResponse;
import org.psu.spacetraders.dto.ShipType;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testutils.TestUtils;

/**
 * Tests for {@link ShipPurchaseManager}
 */
@ExtendWith(MockitoExtension.class)
public class ShipPurchaseManagerTest {

	@Mock
	private ShipyardManager shipyardManager;

	@Mock
	private RefuelPathCalculator refuelPathCalculator;

	@Mock
	private NavigationHelper navigationHelper;

	@Mock
	private MarketplaceRequester marketplaceRequester;

	@Mock
	private ShipJobCreator shipJobCreator;

	@InjectMocks
	private ShipPurchaseManager shipPurchaseManager;

	/**
	 * Tests createShipPurchaseJob where there is no viable route to a shipyard
	 */
	@Test
	public void createShipPurchaseJobNoRoute() {

		final Waypoint way1 = mock();
		final Waypoint way2 = mock();

		final Ship ship = mock();
		final ShipType shipType = ShipType.SHIP_ORE_HOUND;

		when(shipyardManager.getShipyardsWhichSell(shipType)).thenReturn(List.of(way1, way2));

		when(refuelPathCalculator.determineShortestRoute(ship, way1)).thenReturn(null);
		when(refuelPathCalculator.determineShortestRoute(ship, way2)).thenReturn(null);

		final ShipPurchaseJob job = shipPurchaseManager.createShipPurchaseJob(ship, shipType);

		assertNull(job);
	}

	/**
	 * Tests createShipPurchaseJob
	 */
	@Test
	public void createShipPurchaseJob() {

		final Waypoint way1 = mock();
		final Waypoint way2 = mock();

		final Ship ship = mock();
		final ShipType shipType = ShipType.SHIP_ORE_HOUND;

		when(shipyardManager.getShipyardsWhichSell(shipType)).thenReturn(List.of(way1, way2));

		final NavigationPath navPath1 = mock();
		when(navPath1.getLength()).thenReturn(10.0);
		when(refuelPathCalculator.determineShortestRoute(ship, way1)).thenReturn(navPath1);

		// Nav Path 2 is shorter
		final NavigationPath navPath2 = mock();
		when(navPath2.getWaypoints()).thenReturn(TestUtils.makeQueue(way2));
		when(navPath2.getLength()).thenReturn(5.0);
		when(refuelPathCalculator.determineShortestRoute(ship, way2)).thenReturn(navPath2);

		final ShipPurchaseJob job = shipPurchaseManager.createShipPurchaseJob(ship, shipType);

		assertEquals(ship, job.getShip());
		assertEquals(shipType, job.getShipTypeToPurchase());
		assertEquals(way2, job.getShipyard());
		assertEquals(1, job.getPathToShipyard().size());
		assertEquals(way2, job.getPathToShipyard().remove());
	}

	/**
	 * Tests manageShipPurchase while the ship has finished its route
	 */
	@Test
	public void manageShipPurchaseEndOfRoute() {

		final Ship ship = mock();
		final ShipType shipType = ShipType.SHIP_ORE_HOUND;
		final Deque<Waypoint> path = TestUtils.makeQueue();

		final String shipyardId = "Zaxnar's Used Spaceship Emporium";
		final Waypoint shipyard = mock();
		when(shipyard.getSymbol()).thenReturn(shipyardId);

		final Ship newShip = mock();
		final ShipPurchaseResponse response = mock();
		when(response.getShip()).thenReturn(newShip);

		final ShipJob newJob = mock();

		when(shipJobCreator.createShipJob(newShip)).thenReturn(newJob);

		final ShipPurchaseRequest expectedPurchaseRequest = new ShipPurchaseRequest(shipType, shipyardId);
		when(shipyardManager.purchaseShip(expectedPurchaseRequest)).thenReturn(response);

		final ShipPurchaseJob job = new ShipPurchaseJob(ship, path, shipyard, shipType, Instant.now());

		final ShipJob nextJob = shipPurchaseManager.manageShipPurchase(job);

		assertEquals(newJob, nextJob);

		verify(navigationHelper).dock(ship);
	}

	/**
	 * Tests manageShipPurchase while the ship is at the shipyard but has a
	 * non-empty route (typically happens when the ship starts at the shipyard)
	 */
	@Test
	public void manageShipPurchaseAtShipyard() {

		final String shipyardId = "Zaxnar's Used Spaceship Emporium";
		final Waypoint shipyard = mock();
		when(shipyard.getSymbol()).thenReturn(shipyardId);
		final Deque<Waypoint> path = TestUtils.makeQueue(shipyard);

		final Ship ship = mock();
		final ShipType shipType = ShipType.SHIP_ORE_HOUND;
		final ShipNavigation shipNav = mock();
		when(shipNav.getWaypointSymbol()).thenReturn(shipyardId);
		when(ship.getNav()).thenReturn(shipNav);

		final Ship newShip = mock();
		final ShipPurchaseResponse response = mock();
		when(response.getShip()).thenReturn(newShip);

		final ShipJob newJob = mock();

		when(shipJobCreator.createShipJob(newShip)).thenReturn(newJob);

		final ShipPurchaseRequest expectedPurchaseRequest = new ShipPurchaseRequest(shipType, shipyardId);
		when(shipyardManager.purchaseShip(expectedPurchaseRequest)).thenReturn(response);

		final ShipPurchaseJob job = new ShipPurchaseJob(ship, path, shipyard, shipType, Instant.now());

		final ShipJob nextJob = shipPurchaseManager.manageShipPurchase(job);

		assertEquals(newJob, nextJob);

		verify(navigationHelper).dock(ship);
	}

	/**
	 * Tests manageShipPurchase while the ship is traveling
	 */
	@Test
	public void manageShipPurchaseTraveling() {

		final String shipyardId = "Zaxnar's Used Spaceship Emporium";
		final Waypoint shipyard = mock();
		when(shipyard.getSymbol()).thenReturn(shipyardId);
		// Only point left in the path is the shipyard
		final Deque<Waypoint> path = TestUtils.makeQueue(shipyard);

		final Ship ship = mock();
		final ShipType shipType = ShipType.SHIP_ORE_HOUND;
		final ShipNavigation shipNav = mock();
		when(shipNav.getWaypointSymbol()).thenReturn("some other waypoint");
		when(ship.getNav()).thenReturn(shipNav);

		final Instant nextAction = Instant.ofEpochSecond(100);
		when(navigationHelper.navigate(ship, shipyard)).thenReturn(nextAction);

		final ShipPurchaseJob job = new ShipPurchaseJob(ship, path, shipyard, shipType, Instant.now());

		final ShipJob nextJob = shipPurchaseManager.manageShipPurchase(job);

		assertEquals(nextAction, nextJob.getNextAction());
		verify(marketplaceRequester).refuel(ship);
	}

}
