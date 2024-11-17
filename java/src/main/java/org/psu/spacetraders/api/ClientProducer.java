package org.psu.spacetraders.api;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.psu.testdriver.client.LocalAgentClient;
import org.psu.testdriver.client.LocalMarketplaceClient;
import org.psu.testdriver.client.LocalNavigationClient;
import org.psu.testdriver.client.LocalShipsClient;
import org.psu.testdriver.client.LocalSurveyClient;
import org.psu.testdriver.client.LocalWaypointsClient;

import io.quarkus.runtime.configuration.ConfigUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Produces REST Clients or their local variants based on the selected test profile
 */
@ApplicationScoped
public class ClientProducer {

	@RestClient
	private AgentClient agentClient;
	@Inject
	private LocalAgentClient localAgentClient;

	@RestClient
	private MarketplaceClient marketplaceClient;
	@Inject
	private LocalMarketplaceClient localMarketplaceClient;

	@RestClient
	private NavigationClient navigationClient;
	@Inject
	private LocalNavigationClient localNavigationClient;

	@RestClient
	private ShipsClient shipsClient;
	@Inject
	private LocalShipsClient localShipsClient;

	@RestClient
	private SurveyClient surveyClient;
	@Inject
	private LocalSurveyClient localSurveyClient;

	@RestClient
	private WaypointsClient waypointsClient;
	@Inject
	private LocalWaypointsClient localWaypointsClient;

	public AgentClient produceAgentClient() {
		return ConfigUtils.getProfiles().contains("test-driver") ? localAgentClient : agentClient;
	}

	public MarketplaceClient produceMarketplaceClient() {
		return ConfigUtils.getProfiles().contains("test-driver") ? localMarketplaceClient : marketplaceClient;
	}

	public NavigationClient produceNavigationClient() {
		return ConfigUtils.getProfiles().contains("test-driver") ? localNavigationClient : navigationClient;
	}

	public ShipsClient produceShipsClient() {
		return ConfigUtils.getProfiles().contains("test-driver") ? localShipsClient : shipsClient;
	}

	public SurveyClient produceSurveyClient() {
		return ConfigUtils.getProfiles().contains("test-driver") ? localSurveyClient : surveyClient;
	}

	public WaypointsClient produceWaypointsClient() {
		return ConfigUtils.getProfiles().contains("test-driver") ? localWaypointsClient : waypointsClient;
	}
}
