package org.psu.miningmanager;

import java.time.Instant;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.psu.miningmanager.dto.MiningShipJob;
import org.psu.miningmanager.dto.MiningShipJob.State;
import org.psu.miningmanager.dto.SurveyResponse;
import org.psu.spacetraders.api.NavigationHelper;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.api.SurveyClient;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.Waypoint;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Executes actions for mining ships
 */
@JBossLog
@ApplicationScoped
public class MiningShipManager {

	private SurveyClient surveyClient;
	private RequestThrottler throttler;
	private MiningSiteManager siteManager;
	private NavigationHelper navHelper;

	@Inject
	public MiningShipManager(@RestClient final SurveyClient surveyClient, final RequestThrottler throttler,
			final MiningSiteManager siteManager, final NavigationHelper navHelper) {
		this.surveyClient = surveyClient;
		this.throttler = throttler;
		this.siteManager = siteManager;
		this.navHelper = navHelper;
	}

	public MiningShipJob createJob(final Ship ship) {
		final Waypoint extractionSite = siteManager.getClosestMiningSite(ship).get();
		return new MiningShipJob(ship, extractionSite);
	}

	public MiningShipJob manageMiningShip(final MiningShipJob job) {
		final Ship ship = job.getShip();
		final String shipId = ship.getSymbol();

		// The state of the job indicates what is was doing before it reached the manager
		switch (job.getState()) {
		case NOT_STARTED:
			log.infof("Ship %s traveling to extraction waypoint %s", shipId,
					job.getExtractionPoint().getSymbol());
			final Instant extractionArrival = navHelper.navigate(ship, job.getExtractionPoint());
			job.setNextAction(extractionArrival);
			job.setState(State.TRAVELING_TO_RESOURCE);
			break;
		case TRAVELING_TO_RESOURCE:
			final SurveyResponse surveyResponse = throttler.throttle(() -> surveyClient.survey(shipId)).getData();
			job.setSurveys(surveyResponse.getSurveys());
			job.setNextAction(surveyResponse.getCooldown().getExpiration());
			job.setState(State.SURVEYING);
			log.infof("Finished Surveying, found %s sites, ship in cooldown until %s", job.getSurveys().size(),
					job.getNextAction());
			break;
		case SURVEYING:
			break;
		case EXTRACTING:
			break;
		case TRAVELING_TO_MARKET:
			// Make a whole new job and return it
			return createJob(job.getShip());
		}
		return job;
	}

}
