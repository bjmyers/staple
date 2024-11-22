package org.psu.testdriver.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.psu.miningmanager.dto.ExtractResponse;
import org.psu.miningmanager.dto.Survey;
import org.psu.miningmanager.dto.SurveyResponse;
import org.psu.spacetraders.dto.Cargo;
import org.psu.spacetraders.dto.CargoItem;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipNavigation;
import org.psu.spacetraders.dto.ShipRoute;
import org.psu.spacetraders.dto.Trait;
import org.psu.spacetraders.dto.Trait.Type;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.testdriver.LocalShipManager;
import org.psu.testdriver.LocalWaypointsManager;

/**
 * Tests for {@link LocalSurveyClient}
 */
@ExtendWith(MockitoExtension.class)
public class LocalSurveyClientTest {

	@Mock
	private LocalShipManager shipManager;

	@Mock
	private LocalWaypointsManager waypointsManager;

	@InjectMocks
	private LocalSurveyClient surveyClient;

	/**
	 * Tests survey when the ship hasn't arrived at its destination yet
	 */
	@Test
	public void surveyShipNotArrived() {

		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		final ShipRoute shipRoute = mock();
		// Hasn't arrived yet
		when(shipRoute.getArrival()).thenReturn(Instant.now().plus(Duration.ofDays(1)));
		when(shipNav.getRoute()).thenReturn(shipRoute);
		when(ship.getNav()).thenReturn(shipNav);

		final String shipId = "shippy";
		when(shipManager.getShip(shipId)).thenReturn(ship);

		assertThrows(IllegalArgumentException.class, () -> surveyClient.survey(shipId));
	}

	/**
	 * Tests survey when the ship isn't at an extractable waypoint
	 */
	@Test
	public void surveyNotExtractableWaypoint() {

		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		final ShipRoute shipRoute = mock();
		when(shipRoute.getArrival()).thenReturn(Instant.now().minus(Duration.ofDays(1)));
		when(shipNav.getRoute()).thenReturn(shipRoute);
		when(ship.getNav()).thenReturn(shipNav);

		final String shipId = "shippy";
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final String wayId = "waypoint";
		when(shipNav.getWaypointSymbol()).thenReturn(wayId);

		final Trait trait = mock();
		// Nothing valuable here
		when(trait.getSymbol()).thenReturn(Type.BARREN);
		final Waypoint waypoint = mock();
		when(waypoint.getTraits()).thenReturn(List.of(trait));
		when(waypointsManager.getWaypoint(wayId)).thenReturn(waypoint);

		assertThrows(IllegalArgumentException.class, () -> surveyClient.survey(shipId));
	}

	/**
	 * Tests survey
	 */
	@Test
	public void survey() {

		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		final ShipRoute shipRoute = mock();
		when(shipRoute.getArrival()).thenReturn(Instant.now().minus(Duration.ofDays(1)));
		when(shipNav.getRoute()).thenReturn(shipRoute);
		when(ship.getNav()).thenReturn(shipNav);

		final String shipId = "shippy";
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final String wayId = "waypoint";
		when(shipNav.getWaypointSymbol()).thenReturn(wayId);

		final Trait trait = mock();
		when(trait.getSymbol()).thenReturn(Type.PRECIOUS_METAL_DEPOSITS);
		final Waypoint waypoint = mock();
		when(waypoint.getSymbol()).thenReturn(wayId);
		when(waypoint.getTraits()).thenReturn(List.of(trait));
		when(waypointsManager.getWaypoint(wayId)).thenReturn(waypoint);

		final DataWrapper<SurveyResponse> surveyResponse = surveyClient.survey(shipId);

		assertNull(surveyResponse.getMeta());

		assertEquals(1, surveyResponse.getData().getSurveys().size());
		final Survey survey = surveyResponse.getData().getSurveys().iterator().next();
		assertEquals(wayId, survey.getSymbol());
	}

	/**
	 * Tests extract when the ship is not at its destination
	 */
	@Test
	public void extractNotArrived() {

		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		final ShipRoute shipRoute = mock();
		// Hasn't arrived yet
		when(shipRoute.getArrival()).thenReturn(Instant.now().plus(Duration.ofDays(1)));
		when(shipNav.getRoute()).thenReturn(shipRoute);
		when(ship.getNav()).thenReturn(shipNav);

		final String shipId = "shippy";
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final Survey survey = mock();

		assertThrows(IllegalArgumentException.class, () -> surveyClient.extractSurvey(shipId, survey));
	}

	/**
	 * Tests extract when the ship is not at the survey point
	 */
	@Test
	public void extractNotAtSurveyPoint() {

		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		final ShipRoute shipRoute = mock();
		when(shipRoute.getArrival()).thenReturn(Instant.now().minus(Duration.ofDays(1)));
		when(shipNav.getRoute()).thenReturn(shipRoute);
		when(ship.getNav()).thenReturn(shipNav);

		final String shipId = "shippy";
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final String wayId = "waypoint";
		when(shipNav.getWaypointSymbol()).thenReturn(wayId);
		final Waypoint waypoint = mock();
		when(waypoint.getSymbol()).thenReturn(wayId);
		when(waypointsManager.getWaypoint(wayId)).thenReturn(waypoint);

		final Survey survey = mock();
		when(survey.getSymbol()).thenReturn("some other waypoint");

		assertThrows(IllegalArgumentException.class, () -> surveyClient.extractSurvey(shipId, survey));
	}

	/**
	 * Tests extract when the survey has expired
	 */
	@Test
	public void extractExpired() {

		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		final ShipRoute shipRoute = mock();
		when(shipRoute.getArrival()).thenReturn(Instant.now().minus(Duration.ofDays(1)));
		when(shipNav.getRoute()).thenReturn(shipRoute);
		when(ship.getNav()).thenReturn(shipNav);

		final String shipId = "shippy";
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final String wayId = "waypoint";
		when(shipNav.getWaypointSymbol()).thenReturn(wayId);
		final Waypoint waypoint = mock();
		when(waypoint.getSymbol()).thenReturn(wayId);
		when(waypointsManager.getWaypoint(wayId)).thenReturn(waypoint);

		final Survey survey = mock();
		when(survey.getSymbol()).thenReturn(wayId);
		when(survey.getExpiration()).thenReturn(Instant.now().minus(Duration.ofDays(1)));

		assertThrows(IllegalArgumentException.class, () -> surveyClient.extractSurvey(shipId, survey));
	}

	/**
	 * Tests extract when the ship already has some of the product in it
	 */
	@Test
	public void extractAlreadyHasProduct() {

		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		final ShipRoute shipRoute = mock();
		when(shipRoute.getArrival()).thenReturn(Instant.now().minus(Duration.ofDays(1)));
		when(shipNav.getRoute()).thenReturn(shipRoute);
		when(ship.getNav()).thenReturn(shipNav);

		final String productId = "GOLD";
		final Product product = new Product(productId);

		// Two remaining spots in the cargo bay
		final CargoItem cargoItem = new CargoItem(productId, 38);
		final Cargo cargo = new Cargo(40, 38, List.of(cargoItem));
		when(ship.getCargo()).thenReturn(cargo);
		when(ship.getRemainingCargo()).thenReturn(2);

		final String shipId = "shippy";
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final String wayId = "waypoint";
		when(shipNav.getWaypointSymbol()).thenReturn(wayId);
		final Waypoint waypoint = mock();
		when(waypoint.getSymbol()).thenReturn(wayId);
		when(waypointsManager.getWaypoint(wayId)).thenReturn(waypoint);

		final Survey survey = mock();
		when(survey.getSymbol()).thenReturn(wayId);
		when(survey.getExpiration()).thenReturn(Instant.now().plus(Duration.ofDays(1)));
		when(survey.getDeposits()).thenReturn(List.of(product));

		final DataWrapper<ExtractResponse> extractResponse = surveyClient.extractSurvey(shipId, survey);

		assertNull(extractResponse.getMeta());

		final Cargo returnedCargo = extractResponse.getData().getCargo();
		assertEquals(cargo, returnedCargo);
		assertEquals(40, cargo.getUnits());
		final CargoItem expectedCargoItem = new CargoItem(productId, 40);
		assertEquals(1, cargo.getInventory().size());
		assertTrue(cargo.getInventory().contains(expectedCargoItem));
	}

	/**
	 * Tests extract when the ship has an empty cargo bay
	 */
	@Test
	public void extractEmpty() {

		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		final ShipRoute shipRoute = mock();
		when(shipRoute.getArrival()).thenReturn(Instant.now().minus(Duration.ofDays(1)));
		when(shipNav.getRoute()).thenReturn(shipRoute);
		when(ship.getNav()).thenReturn(shipNav);

		final String productId = "GOLD";
		final Product product = new Product(productId);

		final Cargo cargo = new Cargo(40, 0, new ArrayList<>());
		when(ship.getCargo()).thenReturn(cargo);
		when(ship.getRemainingCargo()).thenReturn(40);

		final String shipId = "shippy";
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final String wayId = "waypoint";
		when(shipNav.getWaypointSymbol()).thenReturn(wayId);
		final Waypoint waypoint = mock();
		when(waypoint.getSymbol()).thenReturn(wayId);
		when(waypointsManager.getWaypoint(wayId)).thenReturn(waypoint);

		final Survey survey = mock();
		when(survey.getSymbol()).thenReturn(wayId);
		when(survey.getExpiration()).thenReturn(Instant.now().plus(Duration.ofDays(1)));
		when(survey.getDeposits()).thenReturn(List.of(product));

		final DataWrapper<ExtractResponse> extractResponse = surveyClient.extractSurvey(shipId, survey);

		assertNull(extractResponse.getMeta());

		final Cargo returnedCargo = extractResponse.getData().getCargo();
		assertEquals(cargo, returnedCargo);
		assertEquals(5, cargo.getUnits());
		final CargoItem expectedCargoItem = new CargoItem(productId, 5);
		assertEquals(1, cargo.getInventory().size());
		assertTrue(cargo.getInventory().contains(expectedCargoItem));
	}

}
