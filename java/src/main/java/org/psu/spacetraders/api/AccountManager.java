package org.psu.spacetraders.api;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.psu.spacetraders.dto.Agent;
import org.psu.websocket.WebsocketReporter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Stores information regarding the user's account
 */
@JBossLog
@ApplicationScoped
public class AccountManager {

	private AgentClient agentClient;
	private WebsocketReporter creditReporter;

	private Agent agent;

	@Inject
	public AccountManager(@RestClient AgentClient agentClient, final WebsocketReporter creditReporter) {
		this.agentClient = agentClient;
		this.creditReporter = creditReporter;
		this.agent = null;
	}

	/**
	 * Updates the current state of the user's account information
	 * @param agent The new {@link Agent}
	 */
	public void updateAgent(final Agent agent) {
		this.agent = agent;
		creditReporter.updateCreditTotal(this.agent.getCredits());
		log.infof("Updated Credit Total: %s", this.agent.getCredits());
	}

	/**
	 * @return The number of credits that a user has, will use a cached
	 *         {@link Agent} if available
	 */
	public int getCredits() {
		if (this.agent == null) {
			// Lazy load agent information
			this.agent = agentClient.getAgent().getData();
		}
		return this.agent.getCredits();
	}

}
