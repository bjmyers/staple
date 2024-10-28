package org.psu.websocket;

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * Reports updates in the user's credit total
 */
@ApplicationScoped
@ServerEndpoint("/credit-update")
public class CreditReporter {

	private final Set<Session> sessions = new HashSet<>();
	private int creditTotal = 0;

	@OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        sendUpdateToClient(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    public void updateCreditTotal(int newValue) {
    	creditTotal = newValue;
        for (Session session : sessions) {
            sendUpdateToClient(session);
        }
    }

    private void sendUpdateToClient(Session session) {
        session.getAsyncRemote().sendText(String.valueOf(creditTotal));
    }

}
