package org.psu.websocket.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jakarta.websocket.EncodeException;

/**
 * Tests the {@link ShipEventMessageEncoder}
 */
public class ShipEventMessageEncoderTest {

	/**
	 * Tests encode
	 * @throws EncodeException if something goes wrong
	 */
	@Test
	public void encode() throws EncodeException {

		final String shipId = "ship1";
		final String message = "did something cool";
		final ShipEventMessage eventMessage = new ShipEventMessage(shipId, message);

		final ShipEventMessageEncoder encoder = new ShipEventMessageEncoder();

		final String encoded = encoder.encode(eventMessage);

		assertTrue(encoded.contains(shipId));
		assertTrue(encoded.contains(message));
	}

}
