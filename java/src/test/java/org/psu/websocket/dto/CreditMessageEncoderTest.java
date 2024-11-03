package org.psu.websocket.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jakarta.websocket.EncodeException;

/**
 * Tests {@link CreditMessageEncoder}
 */
public class CreditMessageEncoderTest {

	/**
	 * Tests encode
	 * @throws EncodeException if something goes wrong
	 */
	@Test
	public void encode() throws EncodeException {

		final CreditMessage creditMessage = new CreditMessage(100);

		final CreditMessageEncoder encoder = new CreditMessageEncoder();

		final String encoded = encoder.encode(creditMessage);

		assertTrue(encoded.contains("100"));
	}

}
