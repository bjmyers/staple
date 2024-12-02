package org.psu.websocket.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

public class ShipTypeMessageEncoder implements Encoder.Text<ShipTypeMessage> {

    private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String encode(ShipTypeMessage shipTypeMessage) throws EncodeException {
		try {
			return objectMapper.writeValueAsString(shipTypeMessage);
		} catch (JsonProcessingException e) {
			throw new EncodeException(shipTypeMessage, e.getMessage());
		}
	}

}
