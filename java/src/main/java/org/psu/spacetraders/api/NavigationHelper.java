package org.psu.spacetraders.api;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.NavigationRequest;
import org.psu.spacetraders.dto.NavigationResponse;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipNavigation;
import org.psu.spacetraders.dto.Waypoint;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Helper class to perform navigation. This mostly exists to be a wrapper around the {@link NavigationClient}
 */
@JBossLog
@ApplicationScoped
public class NavigationHelper {

	private Duration navigationPad;
	private NavigationClient navigationClient;
	private RequestThrottler throttler;

	@Inject
	public NavigationHelper(@ConfigProperty(name = "app.navigation-pad-ms") final int navigationPad,
			@RestClient final NavigationClient navigationClient, final RequestThrottler throttler) {
		this.navigationPad = Duration.ofMillis(navigationPad);
		this.navigationClient = navigationClient;
		this.throttler = throttler;
	}

	/**
	 * @param ship     The Ship to navigate, note that this object's navigation
	 *                 field will be modified by this method
	 * @param waypoint The Waypoint to navigate to
	 * @return The Instant in which navigation will be completed
	 * @apiNote This method will undock the ship
	 */
	public Instant navigate(final Ship ship, final Waypoint waypoint) {

		final String waypointSymbol = waypoint.getSymbol();
		// If we're currently at the first waypoint, there's no need to navigate to it
		if (!ship.getNav().getWaypointSymbol().equals(waypointSymbol)) {
			final ShipNavigation newNav = orbitAndNavigate(ship.getSymbol(), waypointSymbol);
			ship.setNav(newNav);
			return newNav.getRoute().getArrival().plus(navigationPad);
		}
		// Navigation is done!
		return Instant.now();
	}

	/**
	 * @param ship the ship to dock, note that this object's navigation will be
	 *             modified by this method
	 */
	public void dock(final Ship ship) {
		final ShipNavigation newNav = throttler.throttle(() -> navigationClient.dock(ship.getSymbol())).getData();
		ship.setNav(newNav);
	}

	/**
	 * @param ship the ship to send to orbit, note that this object's navigation will be
	 *             modified by this method
	 */
	public void orbit(final Ship ship) {
		final ShipNavigation newNav = throttler.throttle(() -> navigationClient.orbit(ship.getSymbol())).getData();
		ship.setNav(newNav);
	}

	private ShipNavigation orbitAndNavigate(final String shipId, final String waypointSymbol) {
		throttler.throttle(() -> navigationClient.orbit(shipId));

		final NavigationRequest navRequest = new NavigationRequest(waypointSymbol);
		final DataWrapper<NavigationResponse> navResponse = throttler
				.throttle(() -> navigationClient.navigate(shipId, navRequest));

		final Instant arrivalTime = navResponse.getData().getNav().getRoute().getArrival();
		final Duration tripTime = Duration.between(Instant.now(), arrivalTime);
		log.infof("Navigation of ship %s to %s will take %s", shipId, waypointSymbol, tripTime);

		return navResponse.getData().getNav();
	}

}
