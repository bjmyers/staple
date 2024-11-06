package org.psu.websocket.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

/**
 * Encodes {@link ShipEventMessage}s
 */
public class ShipEventMessageEncoder implements Encoder.Text<ShipEventMessage>  {

    private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String encode(ShipEventMessage eventMessage) throws EncodeException {
		try {
			return objectMapper.writeValueAsString(eventMessage);
		} catch (JsonProcessingException e) {
			throw new EncodeException(eventMessage, e.getMessage());
		}
	}

}
