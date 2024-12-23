package org.psu.shiporchestrator;

import java.time.Instant;

import org.psu.spacetraders.dto.Ship;

/**
 * This is the interface that the jobs for all managers will implement, these
 * objects will be stored in the {@link JobQueue}
 */
public interface ShipJob {

	/**
	 * @return The instant in which the job can be sent back to the relevant ship manager
	 */
	public Instant getNextAction();

	/**
	 * @return The ship that this job is for
	 */
	public Ship getShip();

}
