package org.psu.websocket.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

/**
 * Encodes {@link ShipMessage}s
 */
public class ShipMessageEncoder implements Encoder.Text<ShipMessage>  {

    private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String encode(ShipMessage shipMessage) throws EncodeException {
		try {
			return objectMapper.writeValueAsString(shipMessage);
		} catch (JsonProcessingException e) {
			throw new EncodeException(shipMessage, e.getMessage());
		}
	}

}
