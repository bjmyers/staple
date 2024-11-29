package org.psu.init;

import org.psu.miningmanager.MiningShipManager;
import org.psu.shiporchestrator.ShipJob;
import org.psu.shiporchestrator.ShipRole;
import org.psu.shiporchestrator.ShipRoleManager;
import org.psu.spacetraders.dto.Ship;
import org.psu.trademanager.TradeShipManager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Responsible for creating jobs for ships
 */
@JBossLog
@ApplicationScoped
public class ShipJobCreator {

	@Inject
	private ShipRoleManager shipRoleManager;

	@Inject
	private MiningShipManager miningShipManager;

	@Inject
	private TradeShipManager tradeShipManager;

	/**
	 * @param ship The ship to create a job for
	 * @return The created {@link ShipJob}, or null if the input ship is a probe ship
	 */
	public ShipJob createShipJob(final Ship ship) {
		final ShipRole shipRole = shipRoleManager.determineRole(ship);
		log.infof("Found ship %s with type %s", ship.getSymbol(), shipRole);

		switch (shipRole) {
		case MINING:
			return miningShipManager.createJob(ship);
		case TRADE:
			return tradeShipManager.createJob(ship);
		case PROBE:
			// Probe ship jobs are created via user input, not this job creator
		}
		return null;
	}

}
