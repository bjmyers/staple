package org.psu.init;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.psu.spacetraders.api.SpaceTradersUtils;
import org.psu.spacetraders.api.WaypointsClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.DataWrapper.WrapperMetadata;
import org.psu.spacetraders.dto.Waypoint;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
@ApplicationScoped
public class SystemBuilder {

	private String systemId;
	private int limit;
	private WaypointsClient waypointsClient;

	@Inject
	public SystemBuilder(@ConfigProperty(name = "app.system") final String systemId,
			@ConfigProperty(name = "app.max-items-per-page") final int limit,
			@RestClient WaypointsClient waypointsClient) {
		this.systemId = systemId;
		this.limit = limit;
		this.waypointsClient = waypointsClient;
	}

	/**
	 * This method queries the space traders API for system information
	 */
    void onStartup(@Observes StartupEvent event) {
    	log.info("Initializing the System");

    	final List<Waypoint> systemWaypoints = gatherWaypoints();

    	log.infof("Found %s Waypoints", systemWaypoints.size());
	}

	private List<Waypoint> gatherWaypoints() {
		final DataWrapper<List<Waypoint>> initialPage = waypointsClient.getWaypoints(systemId, limit, 1);
		log.info("Gathered waypoint page 1");

		final WrapperMetadata metaData = initialPage.getMeta();
		final int numPages = SpaceTradersUtils.getTotalNumPages(metaData);

		final List<Waypoint> waypoints = new ArrayList<>(metaData.getTotal());
		waypoints.addAll(initialPage.getData());

		for (int i = 2; i < numPages + 1; i++) {
			//TODO: Remove this when the throttler exists
			try {
				Thread.sleep(Duration.ofSeconds(1));
			} catch (InterruptedException e) {
				log.error("Sleep between pages interrupted");
			}

			final DataWrapper<List<Waypoint>> nextPage = waypointsClient.getWaypoints(systemId, limit, i);
			log.infof("Gathered waypoint page %s", i);
			waypoints.addAll(nextPage.getData());
		}

		return waypoints;
    }

}
