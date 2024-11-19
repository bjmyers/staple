package org.psu.testdriver;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.psu.spacetraders.dto.Ship;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Responsible for loading ships when in local mode
 */
@JBossLog
@ApplicationScoped
public class LocalShipManager {

	private Map<String, Ship> shipsById;

	@Inject
	public LocalShipManager() {
		this.shipsById = null;
	}

	public List<Ship> getShips() {
		if (this.shipsById == null) {
			loadShips();
		}
		return this.shipsById.values().stream().toList();
	}

	public Ship getShip(final String id) {
		if (this.shipsById == null) {
			loadShips();
		}
		return this.shipsById.get(id);
	}

	private void loadShips() {
		this.shipsById = LocalResourceLoader.loadResourceList("/testDriverData/ships.json", Ship.class).stream()
				.collect(Collectors.toMap(s -> s.getSymbol(), Function.identity()));

		log.infof("Local Ship Manager loaded %s ships", this.shipsById.size());
	}

}
