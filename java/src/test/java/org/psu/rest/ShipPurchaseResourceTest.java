package org.psu.rest;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.psu.shiporchestrator.ShipJobQueue;
import org.psu.spacetraders.dto.ShipType;

/**
 * Tests {@link ShipPurchaseResource}
 */
@ExtendWith(MockitoExtension.class)
public class ShipPurchaseResourceTest {

	@Mock
	private ShipJobQueue shipJobQueue;

	@InjectMocks
	private ShipPurchaseResource shipPurchaseResource;

	/**
	 * Tests purchaseShip
	 */
	@Test
	public void purchaseShip() {

		final String input = "SHIP_PROBE";
		shipPurchaseResource.purchaseShip(input);

		verify(shipJobQueue).setShipTypeToBuy(ShipType.SHIP_PROBE);
	}

}
