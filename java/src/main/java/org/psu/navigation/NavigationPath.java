package org.psu.navigation;

import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.psu.spacetraders.dto.Waypoint;

import lombok.Data;

/**
 * A path for ship navigation
 */
@Data
public class NavigationPath {

	private final double length;
	private final Deque<Waypoint> waypoints;

	public static NavigationPath combine(final NavigationPath path1, final NavigationPath path2) {
		final double totalLength = path1.getLength() + path2.getLength();
		final Deque<Waypoint> allWaypoints = Stream.concat(path1.getWaypoints().stream(), path2.getWaypoints().stream())
				.collect(Collectors.toCollection(LinkedList::new));
		return new NavigationPath(totalLength, allWaypoints);
	}

}
