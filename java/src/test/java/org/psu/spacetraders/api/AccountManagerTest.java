package org.psu.spacetraders.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.dto.Agent;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.websocket.CreditReporter;

/**
 * Tests for {@link AccountManager}
 */
public class AccountManagerTest {

	/**
	 * Tests {@link AccountManager#updateAgent}
	 */
	@Test
	public void updateAgent() {

		final AgentClient client = mock(AgentClient.class);
		final CreditReporter creditReporter = mock(CreditReporter.class);
		final AccountManager manager = new AccountManager(client, creditReporter);

		final int credits = 250;
		final Agent agent = mock(Agent.class);
		when(agent.getCredits()).thenReturn(credits);

		manager.updateAgent(agent);
		assertEquals(credits, manager.getCredits());
		verify(creditReporter).updateCreditTotal(credits);
	}

	/**
	 * Tests {@link AccountManager#getCredits} without first updating the agent
	 */
	@Test
	public void getCredits() {

		final AgentClient client = mock(AgentClient.class);
		final CreditReporter creditReporter = mock(CreditReporter.class);
		final AccountManager manager = new AccountManager(client, creditReporter);

		final int credits = 250;
		final Agent agent = mock(Agent.class);
		when(agent.getCredits()).thenReturn(credits);
		when(client.getAgent()).thenReturn(new DataWrapper<Agent>(agent, null));

		assertEquals(250, manager.getCredits());
	}

}
