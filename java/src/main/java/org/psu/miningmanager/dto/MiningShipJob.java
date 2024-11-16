package org.psu.miningmanager.dto;

import java.time.Instant;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
	private Queue<Waypoint> extractionPath;
	private Waypoint extractionPoint;
	private Instant nextAction;
	/**
	 * This will only be populated after surveying has occurred
	 */
	private List<Survey> surveys;
	/**
	 * The selling path and point will only be populated after resources have been extracted
	 */
	private Queue<Waypoint> sellingPath;
	private Waypoint sellingPoint;
	private State state;

	public MiningShipJob(final Ship ship, final Deque<Waypoint> extractionPath) {
		this.ship = ship;
		this.extractionPath = extractionPath;
		this.extractionPoint = extractionPath.peekLast();
		this.nextAction = Instant.now();
		this.surveys = null;
		this.sellingPath = new LinkedList<>();
		this.sellingPoint = null;
		this.state = State.NOT_STARTED;
	}

	public static enum State {
		NOT_STARTED,
		SURVEYING,
		TRAVELING_TO_RESOURCE,
		EXTRACTING,
		TRAVELING_TO_MARKET
	}
}
