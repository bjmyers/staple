package org.psu.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A websocket message that indicates the total number of credits that the user has
 */
@Data
@AllArgsConstructor
public class CreditMessage {

	private final String type = "Credit";
	private int totalCredits;

}
