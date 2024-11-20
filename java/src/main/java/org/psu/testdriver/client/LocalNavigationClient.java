package org.psu.testdriver.client;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.psu.spacetraders.api.NavigationClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.DockResponse;
import org.psu.spacetraders.dto.FuelStatus;
import org.psu.spacetraders.dto.NavigationRequest;
import org.psu.spacetraders.dto.NavigationResponse;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipNavigation;
import org.psu.spacetraders.dto.ShipNavigation.Status;
import org.psu.spacetraders.dto.ShipRoute.RoutePoint;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testdriver.LocalShipManager;
import org.psu.testdriver.LocalWaypointsManager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Local version of the {@link NavigationClient}
 */
@JBossLog
@ApplicationScoped
public class LocalNavigationClient implements NavigationClient {

	@Inject
	private LocalShipManager shipManager;

	@Inject
	private LocalWaypointsManager waypointsManager;

	@ConfigProperty(name = "app.test-driver.nav-time-ms-per-unit")
	private int navDelayMsPerUnit;

	@Override
	public DataWrapper<DockResponse> orbit(String shipId) {
		final Ship ship = shipManager.getShip(shipId);
		ship.getNav().setStatus(Status.IN_ORBIT);
		return new DataWrapper<DockResponse>(new DockResponse(ship.getNav()), null);
	}

	@Override
	public DataWrapper<DockResponse> dock(String shipId) {
		final Ship ship = shipManager.getShip(shipId);
		ship.getNav().setStatus(Status.DOCKED);
		return new DataWrapper<DockResponse>(new DockResponse(ship.getNav()), null);
	}

	@Override
	public DataWrapper<NavigationResponse> navigate(String shipId, NavigationRequest navRequest) {
		final Ship ship = shipManager.getShip(shipId);
		final ShipNavigation shipNav = ship.getNav();
		final Waypoint destination = waypointsManager.getWaypoint(navRequest.getWaypointSymbol());

		// Determine if ship is at its destination
		if (shipNav.getRoute().getArrival().isAfter(Instant.now())) {
			throw new IllegalStateException("Ship has not yet arrived at its destination");
		}

		// Determine distance required
		final double dist = waypointsManager.getWaypoint(shipNav.getWaypointSymbol()).distTo(destination);
		log.infof("Ship at %s", shipNav.getWaypointSymbol());
		log.infof("Destination %s", navRequest.getWaypointSymbol());
		if (dist > ship.getFuel().current()) {
			throw new IllegalStateException("Ship does not have enough fuel to travel " + dist + " units");
		}

		// The navigation is acceptable!
		final FuelStatus newFuel = new FuelStatus(ship.getFuel().current() - ((int) Math.ceil(dist)),
				ship.getFuel().capacity());
		ship.setFuel(newFuel);

		shipNav.getRoute().setOrigin(shipNav.getRoute().getDestination());
		shipNav.getRoute()
				.setDestination(new RoutePoint(destination.getSymbol(), destination.getX(), destination.getY()));
		shipNav.setWaypointSymbol(destination.getSymbol());

		final Instant departureTime = Instant.now();
		final Duration travelTime = Duration.ofMillis(1).multipliedBy(navDelayMsPerUnit);
		shipNav.getRoute().setDepartureTime(departureTime);
		shipNav.getRoute().setArrival(departureTime.plus(travelTime));

		return new DataWrapper<NavigationResponse>(new NavigationResponse(shipNav, newFuel), null);
	}

}
