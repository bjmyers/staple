package org.psu.testdriver.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.testdriver.LocalMarketplaceManager;

/**
 * Tests for {@link LocalMarketplaceClient}
 */
@ExtendWith(MockitoExtension.class)
public class LocalMarketplaceClientTest {

	@Mock
	private LocalMarketplaceManager marketplaceManager;

	@InjectMocks
	private LocalMarketplaceClient localMarketplaceClient;

	/**
	 * Tests the getMarketInfo method
	 */
	@Test
	public void getMarketInfo() {
		final String marketId = "market";
		final MarketInfo market = mock();
		when(marketplaceManager.getMarketInfo(marketId)).thenReturn(market);

		final DataWrapper<MarketInfo> response = localMarketplaceClient.getMarketInfo("system", marketId);
		assertNull(response.getMeta());
		assertEquals(market, response.getData());
	}

}
