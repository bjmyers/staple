package org.psu.websocket;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.psu.shiporchestrator.ShipRole;
import org.psu.shiporchestrator.ShipRoleManager;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipType;
import org.psu.websocket.dto.CreditMessage;
import org.psu.websocket.dto.PurchaseStatusMessage;
import org.psu.websocket.dto.ShipEventMessage;
import org.psu.websocket.dto.ShipMessage;
import org.psu.websocket.dto.ShipMessage.ShipMessageData;
import org.psu.websocket.dto.ShipTypeMessage;

import jakarta.websocket.RemoteEndpoint.Async;
import jakarta.websocket.Session;

/**
 * Tests for {@link WebsocketReporter}
 */
@ExtendWith(MockitoExtension.class)
public class WebsocketReporterTest {

	@Mock
	private ShipRoleManager shipRoleManager;

	@InjectMocks
	private WebsocketReporter websocketReporter;

	/**
	 * Tests onOpen
	 */
	@Test
	public void onOpen() {

		final Async async1 = mock(Async.class);
		final Session session1 = mock(Session.class);
		when(session1.getAsyncRemote()).thenReturn(async1);

		final int creditTotal = 100;

		final String shipId = "ship";
		final Ship ship = mock(Ship.class);
		when(shipRoleManager.determineRole(ship)).thenReturn(ShipRole.TRADE);
		when(ship.getSymbol()).thenReturn(shipId);

		websocketReporter.updateCreditTotal(creditTotal);
		websocketReporter.updateShips(List.of(ship));

		websocketReporter.onOpen(session1);

		final CreditMessage expectedCreditMessage = new CreditMessage(creditTotal);
		final ShipMessage expectedShipMessage = new ShipMessage(List.of(new ShipMessageData(shipId, ShipRole.TRADE)));

		verify(async1).sendObject(expectedCreditMessage);
		verify(async1).sendObject(expectedShipMessage);
	}

	/**
	 * Tests updateCreditTotal
	 */
	@Test
	public void updateCreditTotal() {

		final Async async1 = mock(Async.class);
		final Session session1 = mock(Session.class);
		when(session1.getAsyncRemote()).thenReturn(async1);

		final Async async2 = mock(Async.class);
		final Session session2 = mock(Session.class);
		when(session2.getAsyncRemote()).thenReturn(async2);

		final int creditTotal = 100;

		websocketReporter.onOpen(session1);
		websocketReporter.onOpen(session2);

		// Update the total after we've established two connections
		websocketReporter.updateCreditTotal(creditTotal);

		final CreditMessage expectedMessage = new CreditMessage(creditTotal);

		verify(async1).sendObject(expectedMessage);
		verify(async2).sendObject(expectedMessage);
	}

	@Test
	public void updateShips() {

		final Async async1 = mock(Async.class);
		final Session session1 = mock(Session.class);
		when(session1.getAsyncRemote()).thenReturn(async1);

		final Async async2 = mock(Async.class);
		final Session session2 = mock(Session.class);
		when(session2.getAsyncRemote()).thenReturn(async2);

		final String shipId = "ship";
		final Ship ship = mock(Ship.class);
		when(shipRoleManager.determineRole(ship)).thenReturn(ShipRole.TRADE);
		when(ship.getSymbol()).thenReturn(shipId);

		websocketReporter.onOpen(session1);
		websocketReporter.onOpen(session2);

		// Update the ships after we've established two connections
		websocketReporter.updateShips(List.of(ship));

		final ShipMessage expectedShipMessage = new ShipMessage(List.of(new ShipMessageData(shipId, ShipRole.TRADE)));

		verify(async1).sendObject(expectedShipMessage);
		verify(async2).sendObject(expectedShipMessage);
	}

	/**
	 * Tests the addShip method
	 */
	@Test
	public void addShip() {

		final Async async1 = mock(Async.class);
		final Session session1 = mock(Session.class);
		when(session1.getAsyncRemote()).thenReturn(async1);

		final Async async2 = mock(Async.class);
		final Session session2 = mock(Session.class);
		when(session2.getAsyncRemote()).thenReturn(async2);

		final String shipId = "ship";
		final Ship ship = mock(Ship.class);
		when(shipRoleManager.determineRole(ship)).thenReturn(ShipRole.TRADE);
		when(ship.getSymbol()).thenReturn(shipId);
		final List<Ship> ships = new ArrayList<>();
		ships.add(ship);

		final String shipId2 = "ship2";
		final Ship ship2 = mock();
		when(ship2.getSymbol()).thenReturn(shipId2);
		when(shipRoleManager.determineRole(ship2)).thenReturn(ShipRole.MINING);

		// Load the first ship before establishing the connections
		websocketReporter.updateShips(ships);

		websocketReporter.onOpen(session1);
		websocketReporter.onOpen(session2);

		// Now add the new ship
		websocketReporter.addShip(ship2);

		final ShipMessage expectedShipMessage = new ShipMessage(List.of(new ShipMessageData(shipId, ShipRole.TRADE),
				new ShipMessageData(shipId2, ShipRole.MINING)));

		verify(async1).sendObject(expectedShipMessage);
		verify(async2).sendObject(expectedShipMessage);
	}

	/**
	 * Tests the addShipTypes method
	 */
	@Test
	public void addShipTypes() {

		final Async async1 = mock(Async.class);
		final Session session1 = mock(Session.class);
		when(session1.getAsyncRemote()).thenReturn(async1);

		final Async async2 = mock(Async.class);
		final Session session2 = mock(Session.class);
		when(session2.getAsyncRemote()).thenReturn(async2);

		final List<ShipType> shipTypes = List.of(ShipType.SHIP_HEAVY_FREIGHTER, ShipType.SHIP_SIPHON_DRONE);

		websocketReporter.onOpen(session1);
		websocketReporter.onOpen(session2);

		// Update the ships after we've established two connections
		websocketReporter.addShipTypes(shipTypes);

		final ShipTypeMessage expectedShipTypeMessage = new ShipTypeMessage(shipTypes);

		verify(async1).sendObject(expectedShipTypeMessage);
		verify(async2).sendObject(expectedShipTypeMessage);
	}

	/**
	 * Tests onClose
	 */
	@Test
	public void onClose() {

		final Async async1 = mock(Async.class);
		final Session session1 = mock(Session.class);
		when(session1.getAsyncRemote()).thenReturn(async1);

		final Async async2 = mock(Async.class);
		final Session session2 = mock(Session.class);
		when(session2.getAsyncRemote()).thenReturn(async2);

		final int creditTotal = 100;

		websocketReporter.onOpen(session1);
		websocketReporter.onOpen(session2);

		websocketReporter.onClose(session2);

		// Update the total, we only have session1 as an active session
		websocketReporter.updateCreditTotal(creditTotal);

		final CreditMessage expectedMessage = new CreditMessage(creditTotal);

		verify(async1).sendObject(expectedMessage);
		verify(async2, never()).sendObject(expectedMessage);
	}

	/**
	 * Tests fireShipEvent
	 */
	@Test
	public void fireShipEvent() {

		final Async async1 = mock(Async.class);
		final Session session1 = mock(Session.class);
		when(session1.getAsyncRemote()).thenReturn(async1);

		final Async async2 = mock(Async.class);
		final Session session2 = mock(Session.class);
		when(session2.getAsyncRemote()).thenReturn(async2);

		websocketReporter.onOpen(session1);
		websocketReporter.onOpen(session2);

		final String shipId = "shippy";
		final String message = "was a very good ship";

		websocketReporter.fireShipEvent(shipId, message);

		final ShipEventMessage expectedMessage = new ShipEventMessage(shipId, message);

		verify(async1).sendObject(expectedMessage);
		verify(async2).sendObject(expectedMessage);
	}

	/**
	 * Tests firePurchaseStatusEvent
	 */
	@Test
	public void firePurchaseStatusEvent() {

		final Async async1 = mock(Async.class);
		final Session session1 = mock(Session.class);
		when(session1.getAsyncRemote()).thenReturn(async1);

		final Async async2 = mock(Async.class);
		final Session session2 = mock(Session.class);
		when(session2.getAsyncRemote()).thenReturn(async2);

		websocketReporter.onOpen(session1);
		websocketReporter.onOpen(session2);

		final String message = "Shippy-1 was purchased!";

		websocketReporter.firePurchaseStatusEvent(message);

		final PurchaseStatusMessage expectedMessage = new PurchaseStatusMessage(message);

		verify(async1).sendObject(expectedMessage);
		verify(async2).sendObject(expectedMessage);
	}

}
