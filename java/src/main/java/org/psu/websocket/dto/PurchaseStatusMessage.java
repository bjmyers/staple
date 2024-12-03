package org.psu.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Message indicating the status of a ship purchase job
 */
@Data
@AllArgsConstructor
public class PurchaseStatusMessage {

	private final String type = "purchaseStatus";
	private String message;
}
