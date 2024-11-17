package org.psu.testdriver.client;

import java.util.List;

import org.psu.spacetraders.api.ShipsClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.DataWrapper.WrapperMetadata;
import org.psu.spacetraders.dto.Ship;
import org.psu.testdriver.LocalShipManager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Local version of the {@link ShipsClient}
 */
@ApplicationScoped
public class LocalShipsClient implements ShipsClient {

	@Inject
	private LocalShipManager shipManager;

	@Override
	public DataWrapper<List<Ship>> getShips(int limit, int page) {
		final List<Ship> ships = shipManager.getShips();
		final WrapperMetadata metadata = new WrapperMetadata(ships.size(), page, limit);
		if (page == 1) {
			return new DataWrapper<>(ships, metadata);
		}
		return new DataWrapper<>(List.of(), metadata);
	}

}
