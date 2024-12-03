package org.psu.websocket.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jakarta.websocket.EncodeException;

/**
 * Tests {@link PurchaseStatusMessageEncoder}
 */
public class PurchaseStatusMessageEncoderTest {

	/**
	 * Tests encode
	 * @throws EncodeException if something goes wrong
	 */
	@Test
	public void encode() throws EncodeException {

		final PurchaseStatusMessage statusMessage = new PurchaseStatusMessage("test");

		final PurchaseStatusMessageEncoder encoder = new PurchaseStatusMessageEncoder();

		final String encoded = encoder.encode(statusMessage);

		assertTrue(encoded.contains("test"));
	}

}
