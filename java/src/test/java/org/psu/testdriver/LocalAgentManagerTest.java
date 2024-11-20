package org.psu.testdriver;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LocalAgentManager}
 */
public class LocalAgentManagerTest {

	/**
	 * Tests the getAgent method
	 */
	@Test
	public void getAgent() {

		final LocalAgentManager localAgentManager = new LocalAgentManager();

		assertNotNull(localAgentManager.getAgent());
		// Test lazy loading path
		assertNotNull(localAgentManager.getAgent());
	}

}
