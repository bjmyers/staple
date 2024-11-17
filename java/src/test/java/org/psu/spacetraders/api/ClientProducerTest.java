package org.psu.spacetraders.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.psu.testdriver.client.LocalAgentClient;
import org.psu.testdriver.client.LocalMarketplaceClient;
import org.psu.testdriver.client.LocalNavigationClient;
import org.psu.testdriver.client.LocalShipsClient;
import org.psu.testdriver.client.LocalSurveyClient;
import org.psu.testdriver.client.LocalWaypointsClient;

/**
 * Tests for {@link ClientProducer}
 */
@ExtendWith(MockitoExtension.class)
public class ClientProducerTest {

	@Mock
	private AgentClient agentClient;
	@Mock
	private LocalAgentClient localAgentClient;

	@Mock
	private MarketplaceClient marketplaceClient;
	@Mock
	private LocalMarketplaceClient localMarketplaceClient;

	@Mock
	private NavigationClient navigationClient;
	@Mock
	private LocalNavigationClient localNavigationClient;

	@Mock
	private ShipsClient shipsClient;
	@Mock
	private LocalShipsClient localShipsClient;

	@Mock
	private SurveyClient surveyClient;
	@Mock
	private LocalSurveyClient localSurveyClient;

	@Mock
	private WaypointsClient waypointsClient;
	@Mock
	private LocalWaypointsClient localWaypointsClient;

	@InjectMocks
	private ClientProducer clientProducer;

	@Test
	public void nonLocalMode() {

		assertEquals(agentClient, clientProducer.produceAgentClient());
		assertEquals(marketplaceClient, clientProducer.produceMarketplaceClient());
		assertEquals(navigationClient, clientProducer.produceNavigationClient());
		assertEquals(shipsClient, clientProducer.produceShipsClient());
		assertEquals(surveyClient, clientProducer.produceSurveyClient());
		assertEquals(waypointsClient, clientProducer.produceWaypointsClient());
	}

	//TODO: Figure out how to mock the profile so that we can test local mode

}
