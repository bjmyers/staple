package org.psu.testdriver.client;

import org.psu.spacetraders.api.AgentClient;
import org.psu.spacetraders.dto.Agent;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.testdriver.LocalAgentManager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Local version of the {@link AgentClient}
 */
@ApplicationScoped
public class LocalAgentClient implements AgentClient {

	@Inject
	private LocalAgentManager agentManager;

	@Override
	public DataWrapper<Agent> getAgent() {
		return new DataWrapper<>(agentManager.getAgent(), null);
	}

}
