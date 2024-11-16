package org.psu.trademanager.dto;

import java.time.Instant;
import java.util.Queue;

import org.psu.shiporchestrator.ShipJob;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.Waypoint;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A job that can be performed by a trade ship
 */
@Getter
@Setter
@EqualsAndHashCode
public class TradeShipJob implements ShipJob {

	private Ship ship;
	private TradeRoute route;
	/**
	 * The path to take for this trade route. Can be null if the route is being started in the middle
	 */
	private Queue<Waypoint> waypoints;
	private Instant nextAction;
	private State state;

	public TradeShipJob(final Ship ship, final TradeRoute route, final Queue<Waypoint> waypoints) {
		this.ship = ship;
		this.route = route;
		this.waypoints = waypoints;
		this.nextAction = Instant.now();
		this.state = State.NOT_STARTED;
	}

	public static enum State {
		NOT_STARTED,
		TRAVELING
	}

}
