package org.psu.spacetraders.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests for {@link Ship}
 */
public class ShipTest {

	/**
	 * Tests if a ship can be deserialized
	 * @throws IOException
	 * @throws DatabindException
	 * @throws StreamReadException
	 */
	@Test
	public void deserialize() throws StreamReadException, DatabindException, IOException {

		final File jsonFile = new File("src/test/resources/ship.json");
		final ObjectMapper mapper = new ObjectMapper();

		final Ship ship = mapper.readValue(jsonFile, Ship.class);

		assertEquals("THE_SPACE_MAN-1", ship.getSymbol());
		assertEquals(400, ship.getFuel().current());
		assertEquals(400, ship.getFuel().capacity());
		assertEquals(0, ship.getCargo().units());
		assertEquals(40, ship.getCargo().capacity());

		final List<ShipComponent> modules = ship.getModules();
		assertEquals(4, modules.size());
		assertTrue(modules.contains(new ShipComponent("MODULE_CARGO_HOLD_II")));
		assertTrue(modules.contains(new ShipComponent("MODULE_CREW_QUARTERS_I")));
		assertTrue(modules.contains(new ShipComponent("MODULE_MINERAL_PROCESSOR_I")));
		assertTrue(modules.contains(new ShipComponent("MODULE_GAS_PROCESSOR_I")));

		final List<ShipComponent> mounts = ship.getMounts();
		assertEquals(4, mounts.size());
		assertTrue(mounts.contains(new ShipComponent("MOUNT_SENSOR_ARRAY_II")));
		assertTrue(mounts.contains(new ShipComponent("MOUNT_GAS_SIPHON_II")));
		assertTrue(mounts.contains(new ShipComponent("MOUNT_MINING_LASER_II")));
		assertTrue(mounts.contains(new ShipComponent("MOUNT_SURVEYOR_II")));

		final ShipNavigation nav = ship.getNav();
		assertEquals("X1-N57", nav.getSystemSymbol());
		assertEquals("X1-N57-A1", nav.getWaypointSymbol());
		assertEquals(ShipNavigation.Status.DOCKED, nav.getStatus());
		assertEquals(ShipNavigation.FlightMode.CRUISE, nav.getFlightMode());

		final ShipRoute route = nav.getRoute();
		assertEquals("X1-N57-A1", route.getOrigin().getSymbol());
		assertEquals(-11, route.getOrigin().getX());
		assertEquals(23, route.getOrigin().getY());
		assertEquals("X1-N57-A1", route.getDestination().getSymbol());
		assertEquals(-11, route.getDestination().getX());
		assertEquals(23, route.getDestination().getY());
		assertEquals("2024-09-16T23:40:54.067Z", route.getArrival());
		assertEquals("2024-09-16T23:40:54.067Z", route.getDepartureTime());
	}

}
