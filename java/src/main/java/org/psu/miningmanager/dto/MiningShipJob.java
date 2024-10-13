package org.psu.miningmanager.dto;

import java.time.Instant;
import java.util.List;

import org.psu.shiporchestrator.ShipJob;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.Waypoint;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A job that can be performed by a mining ship
 */
@Getter
@Setter
@EqualsAndHashCode
public class MiningShipJob implements ShipJob {

	private Ship ship;
	private Waypoint extractionPoint;
	private Instant nextAction;
	/**
	 * This will only be populated after surveying has occurred
	 */
	private List<Survey> surveys;
	/**
	 * This will only be populated after resources have been extracted
	 */
	private Waypoint sellingWaypoint;
	private State state;

	public MiningShipJob(final Ship ship, final Waypoint extractionPoint) {
		this.ship = ship;
		this.extractionPoint = extractionPoint;
		this.nextAction = Instant.now();
		this.surveys = null;
		this.sellingWaypoint = null;
		this.state = State.TRAVELING_TO_RESOURCE;
	}

	public static enum State {
		TRAVELING_TO_RESOURCE,
		SURVEYING,
		EXTRACTING,
		TRAVELING_TO_MARKET
	}
}
