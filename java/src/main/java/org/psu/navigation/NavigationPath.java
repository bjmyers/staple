package org.psu.navigation;

import java.util.List;
import java.util.stream.Stream;

import org.psu.spacetraders.dto.Waypoint;

import lombok.Data;

/**
 * A path for ship navigation
 */
@Data
public class NavigationPath {

	private final double length;
	private final List<Waypoint> waypoints;

	public static NavigationPath combine(final NavigationPath path1, final NavigationPath path2) {
		final double totalLength = path1.getLength() + path2.getLength();
		final List<Waypoint> allWaypoints = Stream.concat(path1.getWaypoints().stream(), path2.getWaypoints().stream())
				.toList();
		return new NavigationPath(totalLength, allWaypoints);
	}

}
