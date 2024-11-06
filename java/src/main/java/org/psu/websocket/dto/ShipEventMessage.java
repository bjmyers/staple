package org.psu.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A websocket message that indicates a ship has performed an action
 */
@Data
@AllArgsConstructor
public class ShipEventMessage {

	private final String type = "ShipEvent";
	private String shipId;
	private String message;

}
