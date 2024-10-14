package org.psu.miningmanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.psu.miningmanager.dto.Cooldown;
import org.psu.miningmanager.dto.MiningShipJob;
import org.psu.miningmanager.dto.MiningShipJob.State;
import org.psu.miningmanager.dto.Survey;
import org.psu.miningmanager.dto.SurveyResponse;
import org.psu.spacetraders.api.NavigationHelper;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.api.SurveyClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testutils.TestRequestThrottler;


/**
 * Tests for {@link MiningShipManager}
 */
public class MiningShipManagerTest {

	/**
	 * Tests createJob
	 */
	@Test
	public void createJob() {

		final Ship ship = mock(Ship.class);

		final Waypoint way = mock(Waypoint.class);
		final MiningSiteManager miningSiteManager = mock(MiningSiteManager.class);
		when(miningSiteManager.getClosestMiningSite(ship)).thenReturn(Optional.of(way));

		final MiningShipManager manager = new MiningShipManager(null, null, miningSiteManager, null);

		final MiningShipJob job = manager.createJob(ship);

		assertEquals(ship, job.getShip());
		assertEquals(way, job.getExtractionPoint());
		assertEquals(State.NOT_STARTED, job.getState());
	}

	/**
	 * Tests manageMiningShip when the job hasn't been started
	 */
	@Test
	public void manageMiningShipNotStarted() {

		final SurveyClient surveyClient = mock(SurveyClient.class);
		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationHelper navigationHelper = mock(NavigationHelper.class);

		final String shipId = "shippy";
		final Ship ship = mock(Ship.class);
		when(ship.getSymbol()).thenReturn(shipId);

		final String extractionSiteId = "waypoint";
		final Waypoint extractionSite = mock(Waypoint.class);
		when(extractionSite.getSymbol()).thenReturn(extractionSiteId);

		final Instant arrivalTime = Instant.ofEpochSecond(10);
		when(navigationHelper.navigate(ship, extractionSite)).thenReturn(arrivalTime);

		final MiningShipJob job = new MiningShipJob(ship, extractionSite);

		final MiningShipManager manager = new MiningShipManager(surveyClient, throttler, null, navigationHelper);

		final MiningShipJob nextJob = manager.manageMiningShip(job);

		assertEquals(State.TRAVELING_TO_RESOURCE, nextJob.getState());
		assertEquals(arrivalTime, nextJob.getNextAction());
	}

	/**
	 * Tests manageMiningShip when the ship has finished traveling to the resource
	 */
	@Test
	public void manageMiningShipTravelingToResource() {

		final SurveyClient surveyClient = mock(SurveyClient.class);
		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationHelper navigationHelper = mock(NavigationHelper.class);

		final String shipId = "shippy";
		final Ship ship = mock(Ship.class);
		when(ship.getSymbol()).thenReturn(shipId);

		final String extractionSiteId = "waypoint";
		final Waypoint extractionSite = mock(Waypoint.class);
		when(extractionSite.getSymbol()).thenReturn(extractionSiteId);

		final SurveyResponse surveyResponse = mock(SurveyResponse.class);
		final Survey survey = mock(Survey.class);
		when(surveyResponse.getSurveys()).thenReturn(List.of(survey));
		final Instant cooldownTime = Instant.ofEpochSecond(10);
		final Cooldown cooldown = new Cooldown(cooldownTime);
		when(surveyResponse.getCooldown()).thenReturn(cooldown);

		when(surveyClient.survey(shipId)).thenReturn(new DataWrapper<SurveyResponse>(surveyResponse, null));

		final MiningShipJob job = new MiningShipJob(ship, extractionSite);
		job.setState(State.TRAVELING_TO_RESOURCE);

		final MiningShipManager manager = new MiningShipManager(surveyClient, throttler, null, navigationHelper);

		final MiningShipJob nextJob = manager.manageMiningShip(job);

		assertEquals(State.SURVEYING, nextJob.getState());
		assertEquals(cooldownTime, nextJob.getNextAction());
	}

	/**
	 * Tests manageMiningShip when the ship has finished surveying
	 */
	@Test
	public void manageMiningShipSurveying() {

		final SurveyClient surveyClient = mock(SurveyClient.class);
		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationHelper navigationHelper = mock(NavigationHelper.class);

		final String shipId = "shippy";
		final Ship ship = mock(Ship.class);
		when(ship.getSymbol()).thenReturn(shipId);

		final String extractionSiteId = "waypoint";
		final Waypoint extractionSite = mock(Waypoint.class);
		when(extractionSite.getSymbol()).thenReturn(extractionSiteId);

		final MiningShipJob job = new MiningShipJob(ship, extractionSite);
		job.setState(State.SURVEYING);

		final MiningShipManager manager = new MiningShipManager(surveyClient, throttler, null, navigationHelper);

		manager.manageMiningShip(job);
	}

	/**
	 * Tests manageMiningShip when the ship has finished extracting
	 */
	@Test
	public void manageMiningShipExtracting() {

		final SurveyClient surveyClient = mock(SurveyClient.class);
		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationHelper navigationHelper = mock(NavigationHelper.class);

		final String shipId = "shippy";
		final Ship ship = mock(Ship.class);
		when(ship.getSymbol()).thenReturn(shipId);

		final String extractionSiteId = "waypoint";
		final Waypoint extractionSite = mock(Waypoint.class);
		when(extractionSite.getSymbol()).thenReturn(extractionSiteId);

		final MiningShipJob job = new MiningShipJob(ship, extractionSite);
		job.setState(State.EXTRACTING);

		final MiningShipManager manager = new MiningShipManager(surveyClient, throttler, null, navigationHelper);

		manager.manageMiningShip(job);
	}

	/**
	 * Tests manageMiningShip when the ship has finished traveling to market
	 */
	@Test
	public void manageMiningShipTravelingToMarket() {

		final SurveyClient surveyClient = mock(SurveyClient.class);
		final RequestThrottler throttler = TestRequestThrottler.get();
		final NavigationHelper navigationHelper = mock(NavigationHelper.class);
		final MiningSiteManager miningSiteManager = mock(MiningSiteManager.class);

		final String shipId = "shippy";
		final Ship ship = mock(Ship.class);
		when(ship.getSymbol()).thenReturn(shipId);

		final String extractionSiteId = "waypoint";
		final Waypoint extractionSite = mock(Waypoint.class);
		when(extractionSite.getSymbol()).thenReturn(extractionSiteId);

		when(miningSiteManager.getClosestMiningSite(ship)).thenReturn(Optional.of(extractionSite));

		final MiningShipJob job = new MiningShipJob(ship, extractionSite);
		job.setState(State.TRAVELING_TO_MARKET);

		final MiningShipManager manager = new MiningShipManager(surveyClient, throttler, miningSiteManager,
				navigationHelper);

		manager.manageMiningShip(job);
	}

}