package org.psu.websocket;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.psu.shiporchestrator.ShipRole;
import org.psu.shiporchestrator.ShipRoleManager;
import org.psu.spacetraders.dto.Ship;
import org.psu.websocket.dto.CreditMessage;
import org.psu.websocket.dto.ShipEventMessage;
import org.psu.websocket.dto.ShipMessage;
import org.psu.websocket.dto.ShipMessage.ShipMessageData;

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

}
