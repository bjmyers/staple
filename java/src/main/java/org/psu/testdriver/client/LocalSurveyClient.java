package org.psu.testdriver.client;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.psu.miningmanager.dto.Cooldown;
import org.psu.miningmanager.dto.ExtractResponse;
import org.psu.miningmanager.dto.Survey;
import org.psu.miningmanager.dto.SurveyResponse;
import org.psu.spacetraders.api.SurveyClient;
import org.psu.spacetraders.dto.CargoItem;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testdriver.LocalShipManager;
import org.psu.testdriver.LocalWaypointsManager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Local version of the {@link SurveyClient}
 */
@ApplicationScoped
public class LocalSurveyClient implements SurveyClient {

	@Inject
	private LocalShipManager shipManager;

	@Inject
	private LocalWaypointsManager waypointsManager;

	@ConfigProperty(name = "app.test-driver.extraction-cooldown-s")
	private int extractionCooldown;

	@Override
	public DataWrapper<SurveyResponse> survey(String shipId) {

		final Ship ship = shipManager.getShip(shipId);

		if (ship.getNav().getRoute().getArrival().isAfter(Instant.now())) {
			throw new IllegalArgumentException("Ship has not yet arrived at its destination");
		}

		final Waypoint site = waypointsManager.getWaypoint(ship.getNav().getWaypointSymbol());

		if (site.getTraits().stream().noneMatch(t -> t.getSymbol().isValuable())) {
			throw new IllegalArgumentException("Nothing valuable at the current waypoint");
		}

		final String signature = "survey signature";
		final String symbol = site.getSymbol();
		final List<Product> deposits = List.of(new Product("GOLD"));
		final Instant expiration = Instant.now().plus(Duration.ofDays(1));
		final String size = "LARGE";
		final Survey survey = new Survey(signature, symbol, deposits, expiration, size);

		final Cooldown cooldown = new Cooldown(extractionCooldown);
		final SurveyResponse surveyResponse = new SurveyResponse(cooldown, List.of(survey));

		return new DataWrapper<SurveyResponse>(surveyResponse, null);
	}

	@Override
	public DataWrapper<ExtractResponse> extractSurvey(String shipId, Survey survey) {

		final Ship ship = shipManager.getShip(shipId);

		if (ship.getNav().getRoute().getArrival().isAfter(Instant.now())) {
			throw new IllegalArgumentException("Ship has not yet arrived at its destination");
		}

		final Waypoint site = waypointsManager.getWaypoint(ship.getNav().getWaypointSymbol());
		if (!site.getSymbol().equals(survey.getSymbol())) {
			throw new IllegalArgumentException("Ship is not at the survey location");
		}

		if (survey.getExpiration().isBefore(Instant.now())) {
			throw new IllegalArgumentException("Survey has expired");
		}

		final Product productToMine = survey.getDeposits().stream().findAny().get();
		final int remainingCapacity = ship.getRemainingCargo();
		// Try to add 5 more units, but don't go over the ship's capacity
		final int quantityToMine = Math.min(remainingCapacity, 5);

		// Update the ship's cargo
		final Optional<CargoItem> optionalCargoItem = ship.getCargo().getInventory().stream()
				.filter(c -> productToMine.getSymbol().equals(c.getSymbol())).findFirst();
		if (optionalCargoItem.isPresent()) {
			final CargoItem cargoItem = optionalCargoItem.get();
			final int newUnits = cargoItem.getUnits() + quantityToMine;
			cargoItem.setUnits(newUnits);
		}
		else {
			ship.getCargo().getInventory().add(new CargoItem(productToMine.getSymbol(), quantityToMine));
		}
		ship.getCargo()
				.setUnits(ship.getCargo().getInventory().stream().collect(Collectors.summingInt(CargoItem::getUnits)));

		final Cooldown cooldown = new Cooldown(extractionCooldown);
		final ExtractResponse extractResponse = new ExtractResponse(cooldown, ship.getCargo());
		return new DataWrapper<>(extractResponse, null);
	}

}
