package org.psu.shippurchase;

import java.time.Instant;
import java.util.Queue;

import org.psu.shiporchestrator.ShipJob;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipType;
import org.psu.spacetraders.dto.Waypoint;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A job to purchase a ship
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class ShipPurchaseJob implements ShipJob {

	private Ship ship;
	private Queue<Waypoint> pathToShipyard;
	private Waypoint shipyard;
	private ShipType shipTypeToPurchase;
	private Instant nextAction;

}
