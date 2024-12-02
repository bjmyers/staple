package org.psu.websocket.dto;

import java.util.Collection;

import org.psu.spacetraders.dto.ShipType;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Message indicating the types of ships available to be purchased
 */
@Data
@AllArgsConstructor
public class ShipTypeMessage {

	private final String type = "ShipTypes";
	private Collection<ShipType> shipTypes;
}
