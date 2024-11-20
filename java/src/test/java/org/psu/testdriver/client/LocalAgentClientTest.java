package org.psu.testdriver.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.psu.spacetraders.dto.Agent;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.testdriver.LocalAgentManager;

/**
 * Tests for {@link LocalAgentClient}
 */
@ExtendWith(MockitoExtension.class)
public class LocalAgentClientTest {

	@Mock
	private LocalAgentManager agentManager;

	@InjectMocks
	private LocalAgentClient agentClient;

	/**
	 * Tests getAgent
	 */
	@Test
	public void getAgent() {
		final Agent agent = mock();
		when(agentManager.getAgent()).thenReturn(agent);

		final DataWrapper<Agent> actualAgent = agentClient.getAgent();

		assertEquals(agent, actualAgent.getData());
		assertNull(actualAgent.getMeta());
	}

}
