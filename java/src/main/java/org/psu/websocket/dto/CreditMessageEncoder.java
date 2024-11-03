package org.psu.websocket.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

/**
 * Encodes {@link CreditMessage}s
 */
public class CreditMessageEncoder implements Encoder.Text<CreditMessage> {

    private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String encode(CreditMessage creditMessage) throws EncodeException {
		try {
			return objectMapper.writeValueAsString(creditMessage);
		} catch (JsonProcessingException e) {
			throw new EncodeException(creditMessage, e.getMessage());
		}
	}

}
