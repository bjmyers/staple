package org.psu.miningmanager;

import org.psu.miningmanager.dto.MiningShipJob;
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

	private MiningSiteManager siteManager;

	@Inject
	public MiningShipManager(final MiningSiteManager siteManager) {
		this.siteManager = siteManager;
	}

	public MiningShipJob createJob(final Ship ship) {
		final Waypoint extractionSite = siteManager.getClosestMiningSite(ship).get();
		return new MiningShipJob(ship, extractionSite);
	}

	public MiningShipJob manageMiningShip(final MiningShipJob job) {
		return job;
	}

}
