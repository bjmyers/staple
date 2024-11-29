package org.psu.testdriver.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.psu.spacetraders.dto.Agent;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipPurchaseRequest;
import org.psu.spacetraders.dto.ShipPurchaseResponse;
import org.psu.spacetraders.dto.ShipType;
import org.psu.spacetraders.dto.ShipyardResponse;
import org.psu.spacetraders.dto.Trait;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testdriver.LocalAgentManager;
import org.psu.testdriver.LocalShipManager;
import org.psu.testdriver.LocalShipyardManager;
import org.psu.testdriver.LocalWaypointsManager;

/**
 * Test for {@link LocalShipyardClient}
 */
@ExtendWith(MockitoExtension.class)
public class LocalShipyardClientTest {

	@Mock
	private LocalAgentManager agentManager;

	@Mock
	private LocalWaypointsManager waypointsManager;

	@Mock
	private LocalShipManager shipManager;

	@Mock
	private LocalShipyardManager shipyardManager;

	@InjectMocks
	private LocalShipyardClient shipyardClient;

	/**
	 * Tests getShipyardData when the waypoint is not a shipyard
	 */
	@Test
	public void getShipyardDataNotShipyard() {

		final String waypointId = "way";
		final Waypoint notAShipyard = mock();
		when(notAShipyard.getTraits()).thenReturn(List.of());
		when(waypointsManager.getWaypoint(waypointId)).thenReturn(notAShipyard);

		assertThrows(IllegalArgumentException.class, () -> shipyardClient.getShipyardData("system", waypointId));
	}

	/**
	 * Tests getShipyardData when the waypoint is a shipyard
	 */
	@Test
	public void getShipyardData() {

		final String waypointId = "way";
		final Waypoint shipyard = mock();
		when(shipyard.getTraits()).thenReturn(List.of(Trait.SHIPYARD));
		when(waypointsManager.getWaypoint(waypointId)).thenReturn(shipyard);

		when(shipyardManager.getShipTypes()).thenReturn(List.of(ShipType.SHIP_EXPLORER));

		final DataWrapper<ShipyardResponse> data = shipyardClient.getShipyardData("system", waypointId);

		assertNull(data.getMeta());
		final ShipyardResponse response = data.getData();
		assertEquals(waypointId, response.getSymbol());
		assertEquals(1, response.getShipTypes().size());
		assertEquals(ShipType.SHIP_EXPLORER, response.getShipTypes().get(0).getType());
	}

	/**
	 * Tests purchaseShip with a bad type
	 */
	@Test
	public void purchaseShipBadType() {

		final ShipType badType = ShipType.SHIP_COMMAND_FRIGATE;
		when(shipyardManager.getShipOfType(badType)).thenReturn(null);

		final ShipPurchaseRequest request = new ShipPurchaseRequest(badType, "waypoint");

		assertThrows(IllegalArgumentException.class, () -> shipyardClient.purchaseShip(request));
	}

	/**
	 * Tests purchaseShip
	 */
	@Test
	public void purchaseShip() {

		final Ship ship = mock();

		final ShipType shipType = ShipType.SHIP_COMMAND_FRIGATE;
		when(shipyardManager.getShipOfType(shipType)).thenReturn(ship);

		// User already has 3 ships
		when(shipManager.getShips()).thenReturn(List.of(mock(), mock(), mock()));

		final Agent agent = mock();
		when(agentManager.getAgent()).thenReturn(agent);
		when(agent.getCredits()).thenReturn(300_000);

		final ShipPurchaseRequest request = new ShipPurchaseRequest(shipType, "waypoint");

		final DataWrapper<ShipPurchaseResponse> response = shipyardClient.purchaseShip(request);

		assertNull(response.getMeta());
		final ShipPurchaseResponse purchaseResponse = response.getData();
		assertEquals(ship, purchaseResponse.getShip());
		assertEquals(agent, purchaseResponse.getAgent());

		verify(agent).setCredits(200_000);
		verify(shipManager).addShip(ship);
	}

}
