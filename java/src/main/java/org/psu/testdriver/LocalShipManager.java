package org.psu.testdriver;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.psu.spacetraders.dto.Ship;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Responsible for loading ships when in local mode
 */
@JBossLog
@ApplicationScoped
public class LocalShipManager {

	private final ObjectMapper objectMapper;
	private List<Ship> ships;

	@Inject
	public LocalShipManager() {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		this.objectMapper = mapper;

		this.ships = null;
	}

	public List<Ship> getShips() {
		if (this.ships == null) {
			lazyLoadShips();
		}
		return this.ships;
	}

	private void lazyLoadShips() {

		try (final InputStream is = this.getClass().getResourceAsStream("/testDriverData/ships.json")) {
			final JavaType shipList = objectMapper.getTypeFactory().constructCollectionLikeType(List.class, Ship.class);
			try {
				this.ships = objectMapper.readValue(is, shipList);
			} catch (Exception e) {
				log.error(e);
			}
		} catch (IOException e) {
			log.error(e);
		}

		log.infof("Local Ship Manager loaded %s ships", this.ships.size());
	}

}
