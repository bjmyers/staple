package org.psu.testdriver.client;

import org.psu.spacetraders.api.AgentClient;
import org.psu.spacetraders.dto.Agent;
import org.psu.spacetraders.dto.DataWrapper;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Local version of the {@link AgentClient}
 */
@ApplicationScoped
public class LocalAgentClient implements AgentClient {

	@Override
	public DataWrapper<Agent> getAgent() {
		return null;
	}

}
