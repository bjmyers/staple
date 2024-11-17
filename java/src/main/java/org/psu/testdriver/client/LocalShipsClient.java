package org.psu.testdriver.client;

import java.util.List;

import org.psu.spacetraders.api.ShipsClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.Ship;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Local version of the {@link ShipsClient}
 */
@ApplicationScoped
public class LocalShipsClient implements ShipsClient {

	@Override
	public DataWrapper<List<Ship>> getShips(int limit, int page) {
		return null;
	}

}
