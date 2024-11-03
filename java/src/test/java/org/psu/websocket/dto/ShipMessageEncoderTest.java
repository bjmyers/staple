package org.psu.websocket.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.psu.shiporchestrator.ShipRole;
import org.psu.websocket.dto.ShipMessage.ShipMessageData;

import jakarta.websocket.EncodeException;

/**
 * Tests for {@link ShipMessageEncoder}
 */
public class ShipMessageEncoderTest {

	/**
	 * Tests encode
	 * @throws EncodeException if something goes wrong
	 */
	@Test
	public void encode() throws EncodeException {

		final String ship1Id = "ship1";
		final ShipMessageData data1 = new ShipMessageData(ship1Id, ShipRole.MINING);
		final String ship2Id = "ship2";
		final ShipMessageData data2 = new ShipMessageData(ship2Id, ShipRole.TRADE);
		final ShipMessage shipMessage = new ShipMessage(List.of(data1, data2));

		final ShipMessageEncoder encoder = new ShipMessageEncoder();

		final String encoded = encoder.encode(shipMessage);

		assertTrue(encoded.contains(ship1Id));
		assertTrue(encoded.contains(ship2Id));
	}

}
