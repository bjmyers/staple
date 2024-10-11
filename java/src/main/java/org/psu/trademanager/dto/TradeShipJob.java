package org.psu.trademanager.dto;

import java.time.Instant;

import org.psu.spacetraders.dto.Ship;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A job that can be performed by a trade ship
 */
@Getter
@Setter
@EqualsAndHashCode
public class TradeShipJob {

	private Ship ship;
	private TradeRoute route;
	private Instant nextAction;
	private State state;

	public TradeShipJob(final Ship ship, final TradeRoute route) {
		this.ship = ship;
		this.route = route;
		this.nextAction = Instant.now();
		this.state = State.NOT_STARTED;
	}

	public static enum State {
		NOT_STARTED,
		TRAVELING_TO_EXPORT,
		TRAVELING_TO_IMPORT
	}

}
