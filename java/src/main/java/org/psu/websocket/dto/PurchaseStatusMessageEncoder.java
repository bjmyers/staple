package org.psu.websocket.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

/**
 * Encodes {@link PurchaseStatusMessage}s
 */
public class PurchaseStatusMessageEncoder implements Encoder.Text<PurchaseStatusMessage> {

    private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String encode(PurchaseStatusMessage statusMessage) throws EncodeException {
		try {
			return objectMapper.writeValueAsString(statusMessage);
		} catch (JsonProcessingException e) {
			throw new EncodeException(statusMessage, e.getMessage());
		}
	}

}
