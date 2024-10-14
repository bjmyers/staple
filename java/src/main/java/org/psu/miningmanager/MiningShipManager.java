package org.psu.miningmanager;

import java.time.Instant;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.psu.miningmanager.dto.ExtractResponse;
import org.psu.miningmanager.dto.MiningShipJob;
import org.psu.miningmanager.dto.MiningShipJob.State;
import org.psu.miningmanager.dto.Survey;
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
		final Waypoint destination = siteManager.getMiningSite(ship.getNav().getWaypointSymbol());
		if (destination != null) {
			// The ship is at or traveling to a mining site
			if (ship.getNav().getRoute().getArrival().compareTo(Instant.now()) > 0) {
				// The ship will arrive in the future
				final MiningShipJob job = new MiningShipJob(ship, destination);
				job.setState(State.TRAVELING_TO_RESOURCE);
				job.setNextAction(ship.getNav().getRoute().getArrival());
				return job;
			}
			// The ship is already at a mining site
			if (ship.getRemainingCargo() > 0) {
				// We're not full, start by surveying and then start extracting
				final MiningShipJob job = new MiningShipJob(ship, destination);
				job.setState(State.TRAVELING_TO_RESOURCE);
				return job;
			}
			// We're full, let the job calculate the marketplace to sell
			final MiningShipJob job = new MiningShipJob(ship, destination);
			job.setState(State.EXTRACTING);
			return job;
		}
		// The ship is not at a mining site or traveling to one.
		//TODO: Figure out if we're traveling to a marketplace that can buy our cargo

		// We're not in the middle of a mining job, make a new one
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
			log.infof("Finished Surveying, found %s sites, ship %s in cooldown until %s", job.getSurveys().size(),
					shipId, job.getNextAction());
			break;
		case SURVEYING, EXTRACTING:
			// If we're full, finish extracting
			if (ship.getRemainingCargo() == 0) {
				//TODO: Find the closest market
				job.setState(State.TRAVELING_TO_MARKET);
				log.infof("Ship %s finished extracting, ready to extract again at %s",
						shipId, job.getNextAction());
				return job;
			}
			//TODO: Find a better way of swapping between surveys
			log.infof("Ship %s extracting resources", shipId);
			final Survey survey = job.getSurveys().get(0);
			final ExtractResponse extractResponse = throttler.throttle(() -> surveyClient.extractSurvey(shipId, survey))
					.getData();
			ship.setCargo(extractResponse.getCargo());

			job.setNextAction(extractResponse.getCooldown().getExpiration());
			job.setState(State.EXTRACTING);
			log.infof("Ship %s finished extraction, ready to extract again at %s",
					shipId, job.getNextAction());
			break;
		case TRAVELING_TO_MARKET:
			// Make a whole new job and return it
			return createJob(job.getShip());
		}
		return job;
	}

}
