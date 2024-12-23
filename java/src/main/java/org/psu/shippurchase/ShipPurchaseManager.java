package org.psu.shippurchase;

import java.time.Instant;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.psu.navigation.NavigationPath;
import org.psu.navigation.RefuelPathCalculator;
import org.psu.shiporchestrator.ShipJob;
import org.psu.spacetraders.api.MarketplaceRequester;
import org.psu.spacetraders.api.NavigationHelper;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipPurchaseRequest;
import org.psu.spacetraders.dto.ShipPurchaseResponse;
import org.psu.spacetraders.dto.ShipType;
import org.psu.spacetraders.dto.Waypoint;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Manages the purchasing of new ship
 */
@JBossLog
@ApplicationScoped
public class ShipPurchaseManager {

	@Inject
	private ShipyardManager shipyardManager;

	@Inject
	private RefuelPathCalculator refuelPathCalculator;

	@Inject
	private NavigationHelper navigationHelper;

	@Inject
	private MarketplaceRequester marketplaceRequester;

	public ShipPurchaseJob createShipPurchaseJob(final Ship ship, final ShipType shipType) {

		final List<Waypoint> shipyardsWhichSellShip = shipyardManager.getShipyardsWhichSell(shipType);

		final Optional<NavigationPath> shortestPathToShipyard = shipyardsWhichSellShip.stream()
				.map(w -> refuelPathCalculator.determineShortestRoute(ship, w)).filter(Objects::nonNull)
				.min(Comparator.comparing(NavigationPath::getLength));

		if (shortestPathToShipyard.isEmpty()) {
			log.warnf("Unable to find a path to a shipyard which sells %s", shipType);
			return null;
		}

		final Deque<Waypoint> path = shortestPathToShipyard.get().getWaypoints();
		final Instant nextAction = Instant.now();

		return new ShipPurchaseJob(ship, path, path.getLast(), shipType, nextAction);
	}

	/**
	 * @param job The ShipPurchaseJob to progress
	 * @return a ShipPurchaseManagerResponse, only one of the two fields will be
	 *         non-null. If the newShip field is null, then the purchase job has not
	 *         yet been completed and must be sent back to the manager. If the
	 *         nextJob field is null, then the purchase job has been completed, and
	 *         a job can be created for the new ship, and the ship which was
	 *         performing the purchase is free to be assigned a new job
	 */
	public ShipPurchaseManagerResponse manageShipPurchase(final ShipPurchaseJob job) {

		if (job.getPathToShipyard().isEmpty()
				|| job.getShip().getNav().getWaypointSymbol().equals(job.getShipyard().getSymbol())) {
			// We've reached the shipyard
			navigationHelper.dock(job.getShip());

			final ShipPurchaseRequest purchaseRequest = new ShipPurchaseRequest(job.getShipTypeToPurchase(),
					job.getShipyard().getSymbol());

			final ShipPurchaseResponse purchaseResponse = shipyardManager.purchaseShip(purchaseRequest);
			return new ShipPurchaseManagerResponse(null, purchaseResponse.getShip());
		}

		final Waypoint nextWaypoint = job.getPathToShipyard().remove();

		marketplaceRequester.refuel(job.getShip());
		final Instant nextAction = navigationHelper.navigate(job.getShip(), nextWaypoint);
		job.setNextAction(nextAction);

		return new ShipPurchaseManagerResponse(job, null);
	}

	// TODO: I don't like how this manager needs this response object, making the
	// job queue more event based would result in a cleaner architecture, currently
	// we need to pass back too much information to the queue so that it can produce
	// the correct jobs
	public record ShipPurchaseManagerResponse(ShipJob nextJob, Ship newShip) {
	};

}
