package org.psu.trademanager.dto;

import java.time.Instant;
import java.util.List;

import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.TradeRequest;

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
	/**
	 * The purchases made on this trade route, only non-null once in the
	 * TRAVELING_TO_IMPORT state and beyond
	 */
	private List<TradeRequest> purchases;
	private Instant nextAction;
	private State state;

	public TradeShipJob(final Ship ship, final TradeRoute route) {
		this.ship = ship;
		this.route = route;
		this.purchases = null;
		this.nextAction = Instant.now();
		this.state = State.NOT_STARTED;
	}

	public static enum State {
		NOT_STARTED,
		TRAVELING_TO_EXPORT,
		TRAVELING_TO_IMPORT
	}

}
