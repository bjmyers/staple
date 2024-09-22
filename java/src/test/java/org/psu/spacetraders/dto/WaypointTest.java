package org.psu.spacetraders.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests for {@link Waypoint}
 */
public class WaypointTest {

	/**
	 * Tests serialization and deserialization of a waypoint
	 * @throws JsonProcessingException if something goes wrong
	 */
	@Test
	public void serializeDeserialize() throws JsonProcessingException {

		final ObjectMapper mapper = new ObjectMapper();

		final String symbol = "X1-N57-F46";
		final WaypointType type = WaypointType.ASTEROID;
		final List<Orbital> orbitals = List.of(new Orbital("X1-N57-F47"));
		final List<Trait> traits = List.of(new Trait(Trait.Type.BARREN), new Trait(Trait.Type.MARKETPLACE));
		final int x = -47;
		final int y = 32;

		final Waypoint waypoint = new Waypoint(symbol, type, orbitals, traits, x, y);

		final String json = mapper.writeValueAsString(waypoint);

		final Waypoint deserializedWaypoint = mapper.readValue(json, Waypoint.class);

		assertEquals(waypoint.getSymbol(), deserializedWaypoint.getSymbol());
		assertEquals(waypoint.getType(), deserializedWaypoint.getType());
		assertEquals(waypoint.getOrbitals(), deserializedWaypoint.getOrbitals());
		assertEquals(waypoint.getTraits(), deserializedWaypoint.getTraits());
		assertEquals(waypoint.getX(), deserializedWaypoint.getX());
		assertEquals(waypoint.getY(), deserializedWaypoint.getY());

	}

}
