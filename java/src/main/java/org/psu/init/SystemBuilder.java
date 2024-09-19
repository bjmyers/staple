package org.psu.init;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.psu.spacetraders.api.WaypointsClient;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
@ApplicationScoped
public class SystemBuilder {

	@RestClient
	private WaypointsClient waypointsClient;

	@ConfigProperty(name = "app.system")
	private String systemId;

	/**
	 * This method queries the space traders API for system information
	 */
    void onStartup(@Observes StartupEvent event) {
    	log.info("Initializing the System");
    	log.info("Found Waypoints: " + waypointsClient.getWaypoints(systemId));
	}

}
