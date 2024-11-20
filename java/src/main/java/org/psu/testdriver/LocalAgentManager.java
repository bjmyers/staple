package org.psu.testdriver;

import org.psu.spacetraders.dto.Agent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Handles the agent state in local mode
 */
@JBossLog
@ApplicationScoped
public class LocalAgentManager {

	private Agent agent;

	@Inject
	public LocalAgentManager() {
		this.agent = null;
	}

	public Agent getAgent() {
		if (this.agent == null) {
			this.agent = LocalResourceLoader.loadResource("/testDriverData/agent.json", Agent.class);
		}
		return this.agent;
	}

}
