package org.psu.testdriver;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.psu.spacetraders.dto.Waypoint;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Responsible for loading waypoints when in local mode
 */
@JBossLog
@ApplicationScoped
public class LocalWaypointsManager {

	private final ObjectMapper objectMapper;
	private List<Waypoint> waypoints;

	@Inject
	public LocalWaypointsManager() {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		this.objectMapper = mapper;

		this.waypoints = null;
	}

	public List<Waypoint> getWaypoints() {
		if (this.waypoints == null) {
			lazyLoadWaypoints();
		}
		return this.waypoints;
	}

	private void lazyLoadWaypoints() {

		try (final InputStream is = this.getClass().getResourceAsStream("/testDriverData/waypoints.json")) {
			final JavaType waypointList = objectMapper.getTypeFactory().constructCollectionLikeType(List.class,
					Waypoint.class);
			try {
				this.waypoints = objectMapper.readValue(is, waypointList);
			} catch (Exception e) {
				log.error(e);
			}
		} catch (IOException e) {
			log.error(e);
		}

		log.infof("Local Waypoint Manager loaded %s waypoints", this.waypoints.size());
	}

}
