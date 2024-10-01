package org.psu.trademanager.dto;

import java.util.List;

import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Waypoint;

import lombok.Data;

/**
 * A trade route denotes a route where given goods can be traded from one
 * waypoint which exports them, and another which imports them
 */
@Data
public class TradeRoute {

	private final Waypoint exportWaypoint;
	private final Waypoint importWaypoint;
	private final List<Product> goods;

}
