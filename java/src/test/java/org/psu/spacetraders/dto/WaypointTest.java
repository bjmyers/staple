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
 * Tests for {@link Waypoint}
 */
public class WaypointTest {

	/**
	 * Tests if a waypoint can be deserialized
	 * @throws IOException
	 * @throws DatabindException
	 * @throws StreamReadException
	 */
	@Test
	public void deserialize() throws StreamReadException, DatabindException, IOException {

		final File jsonFile = new File("src/test/resources/waypoint.json");
		final ObjectMapper mapper = new ObjectMapper();

		final Waypoint waypoint = mapper.readValue(jsonFile, Waypoint.class);

		assertEquals("X1-N57", waypoint.getSystemSymbol());
		assertEquals("X1-N57-F46", waypoint.getSymbol());
		assertEquals(WaypointType.PLANET, waypoint.getType());
		assertEquals(1, waypoint.getOrbitals().size());
		assertEquals("X1-N57-F47", waypoint.getOrbitals().get(0).getSymbol());
		assertEquals(3, waypoint.getTraits().size());

		final List<Trait.Type> traits = waypoint.getTraits().stream().map(Trait::getSymbol).toList();
		assertTrue(traits.contains(Trait.Type.BARREN));
		assertTrue(traits.contains(Trait.Type.EXPLOSIVE_GASES));
		assertTrue(traits.contains(Trait.Type.MARKETPLACE));

		assertEquals(-11, waypoint.getX());
		assertEquals(74, waypoint.getY());
	}

	/**
	 * Tests if a waypoint can be deserialized when there is an unknown trait
	 * @throws IOException
	 * @throws DatabindException
	 * @throws StreamReadException
	 */
	@Test
	public void deserializeUnknownTrait() throws StreamReadException, DatabindException, IOException {

		final File jsonFile = new File("src/test/resources/waypointWeirdTrait.json");
		final ObjectMapper mapper = new ObjectMapper();

		final Waypoint waypoint = mapper.readValue(jsonFile, Waypoint.class);

		assertEquals(1, waypoint.getTraits().size());
		assertEquals(Trait.Type.UNKNOWN, waypoint.getTraits().get(0).getSymbol());
	}

	/**
	 * Tests {@link Waypoint#distTo}
	 */
	@Test
	public void distTo() {
		final Waypoint way1 = new Waypoint();
		way1.setX(0);
		way1.setY(0);

		final Waypoint way2 = new Waypoint();
		way2.setX(3);
		way2.setY(4);

		assertEquals(5.0, way1.distTo(way2), 1e-9);
		assertEquals(5.0, way2.distTo(way1), 1e-9);
	}

}
