package org.psu.websocket.dto;

import java.util.List;

import org.psu.shiporchestrator.ShipRole;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A websocket message that indicates the ships that the user has
 */
@Data
@AllArgsConstructor
public class ShipMessage {

	private final String type = "Ships";
	private final List<ShipMessageData> ships;

	public record ShipMessageData(String symbol, ShipRole role) {};
}
