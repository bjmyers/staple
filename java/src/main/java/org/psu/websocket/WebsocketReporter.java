package org.psu.websocket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.psu.shiporchestrator.ShipRoleManager;
import org.psu.spacetraders.dto.Ship;
import org.psu.websocket.dto.CreditMessage;
import org.psu.websocket.dto.CreditMessageEncoder;
import org.psu.websocket.dto.ShipEventMessage;
import org.psu.websocket.dto.ShipEventMessageEncoder;
import org.psu.websocket.dto.ShipMessage;
import org.psu.websocket.dto.ShipMessage.ShipMessageData;
import org.psu.websocket.dto.ShipMessageEncoder;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * Reports updates in the user's credit total
 */
@ApplicationScoped
@ServerEndpoint(value = "/staple-update", encoders = { CreditMessageEncoder.class, ShipMessageEncoder.class,
		ShipEventMessageEncoder.class })
public class WebsocketReporter {

	@Inject
	private ShipRoleManager shipRoleManager;

	private final Set<Session> sessions = new HashSet<>();
	private int creditTotal = 0;
	private List<Ship> ships = new ArrayList<>();

	@OnOpen
    public void onOpen(Session session) {
		this.sessions.add(session);
        sendCreditUpdate(session);
        sendShipUpdate(session);
    }

    @OnClose
    public void onClose(Session session) {
    	this.sessions.remove(session);
    }

    public void fireShipEvent(final String shipId, final String message) {
    	final ShipEventMessage eventMessage = new ShipEventMessage(shipId, message);
    	for (Session session : this.sessions) {
    		session.getAsyncRemote().sendObject(eventMessage);
    	}
    }

    public void updateCreditTotal(int newValue) {
    	this.creditTotal = newValue;
        for (Session session : this.sessions) {
            sendCreditUpdate(session);
        }
    }

    public void updateShips(final List<Ship> ships) {
		this.ships = ships;
		for (Session session : this.sessions) {
			sendShipUpdate(session);
		}
	}

    private void sendCreditUpdate(final Session session) {
    	final CreditMessage creditMessage = new CreditMessage(this.creditTotal);
        session.getAsyncRemote().sendObject(creditMessage);
    }

    private void sendShipUpdate(final Session session) {
		final ShipMessage shipMessage = new ShipMessage(
				ships.stream().map(s -> new ShipMessageData(s.getSymbol(), shipRoleManager.determineRole(s))).toList());
		session.getAsyncRemote().sendObject(shipMessage);
    }

}
