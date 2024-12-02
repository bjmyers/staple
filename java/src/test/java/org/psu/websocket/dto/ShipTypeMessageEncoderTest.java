package org.psu.websocket.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.dto.ShipType;

import jakarta.websocket.EncodeException;

/**
 * Tests {@link ShipTypeMessageEncoder}
 */
public class ShipTypeMessageEncoderTest {

	/**
	 * Tests encode
	 * @throws EncodeException if something goes wrong
	 */
	@Test
	public void encode() throws EncodeException {

		final ShipType type1 = ShipType.SHIP_EXPLORER;
		final ShipType type2 = ShipType.SHIP_SURVEYOR;
		final ShipTypeMessage shipTypeMessage = new ShipTypeMessage(List.of(type1, type2));

		final ShipTypeMessageEncoder encoder = new ShipTypeMessageEncoder();

		final String encoded = encoder.encode(shipTypeMessage);

		assertTrue(encoded.contains(type1.toString()));
		assertTrue(encoded.contains(type2.toString()));
	}
}
