package org.psu.navigation;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipRoute.RoutePoint;
import org.psu.spacetraders.dto.Waypoint;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Determines the shortest path between two waypoints, may require stops at points to refuel
 */
@JBossLog
@ApplicationScoped
public class RefuelPathCalculator {

	private SimpleWeightedGraph<Waypoint, DefaultWeightedEdge> refuelGraph;

	@Inject
	public RefuelPathCalculator() {
		this.refuelGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
	}

	public void loadRefuelWaypoints(final List<Waypoint> refuelWaypoints) {
		// Reset the refuel graph
		this.refuelGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

		refuelWaypoints.forEach(w -> this.refuelGraph.addVertex(w));

		// Add weighted edges
		for (final Waypoint way1 : refuelWaypoints) {
			for (final Waypoint way2 : refuelWaypoints) {
				if (way1 != way2) {
					final DefaultWeightedEdge newEdge = this.refuelGraph.addEdge(way1, way2);
					if (newEdge != null) {
						// Non-null indicates that this is a new edge
						this.refuelGraph.setEdgeWeight(newEdge, way1.distTo(way2));
					}
				}
			}
		}

		log.info("Loaded Graph");
	}

	/**
	 * @param ship the ship
	 * @param destination the ship's desired destination
	 * @return A path object containing the total distance to travel and a list of
	 *         waypoints containing the optimal path from the origin to the
	 *         destination, this list will not include the origin. Will return null
	 *         if there is no feasible path to the destination.
	 */
	public NavigationPath determineShortestRoute(final Ship ship, final Waypoint destination) {
		final Waypoint fakeOrigin = new Waypoint();
		final RoutePoint shipPosition = ship.getNav().getRoute().getDestination();
		fakeOrigin.setX(shipPosition.getX());
		fakeOrigin.setY(shipPosition.getY());

		return determineShortestRoute(fakeOrigin, destination, ship.getFuel().current(), ship.getFuel().capacity());
	}

	/**
	 * @param origin       the current ship location
	 * @param destination  the waypoint the ship intends to reach
	 * @param currentFuel  the amount of fuel the ship has currently
	 * @param fuelCapacity the total fuel capacity of the ship
	 * @return A path object containing the total distance to travel and a list of
	 *         waypoints containing the optimal path from the origin to the
	 *         destination, this list will not include the origin. Will return null
	 *         if there is no feasible path to the destination.
	 */
	public NavigationPath determineShortestRoute(final Waypoint origin, final Waypoint destination,
			final int currentFuel, final int fuelCapacity) {

		@SuppressWarnings("unchecked")
		final SimpleWeightedGraph<Waypoint, DefaultWeightedEdge> tempGraph =
				(SimpleWeightedGraph<Waypoint, DefaultWeightedEdge>) this.refuelGraph.clone();
		if (!tempGraph.containsVertex(origin)) {
			addVertex(tempGraph, origin);
		}
		if (!tempGraph.containsVertex(destination)) {
			addVertex(tempGraph, destination);
		}

		final List<DefaultWeightedEdge> edgesToRemove = new ArrayList<>();
		for (final DefaultWeightedEdge edge : tempGraph.edgeSet()) {
			final double edgeWeight = tempGraph.getEdgeWeight(edge);
			final boolean containsOrigin = origin.equals(tempGraph.getEdgeSource(edge))
					|| origin.equals(tempGraph.getEdgeTarget(edge));
			// Remove the edges that the ship cannot traverse from its current location given its current fuel
			// Remove the edges that the ship cannot traverse with its max fuel
			if ((containsOrigin && edgeWeight > currentFuel) || (edgeWeight > fuelCapacity)) {
				edgesToRemove.add(edge);
			}
		}
		tempGraph.removeAllEdges(edgesToRemove);

		final DijkstraShortestPath<Waypoint, DefaultWeightedEdge> dijk = new DijkstraShortestPath<>(tempGraph);
		final GraphPath<Waypoint, DefaultWeightedEdge> shortestPath = dijk.getPath(origin, destination);

		if (shortestPath == null) {
			return null;
		}

		final Deque<Waypoint> waypoints = shortestPath.getVertexList().stream()
				.collect(Collectors.toCollection(LinkedList::new));
		final double totalLength = shortestPath.getWeight();

		// Be sure to strip out the origin
		waypoints.remove();
		return new NavigationPath(totalLength, waypoints);

	}

	private void addVertex(final SimpleWeightedGraph<Waypoint, DefaultWeightedEdge> graph, final Waypoint waypoint) {
		graph.addVertex(waypoint);
		for (final Waypoint way : graph.vertexSet()) {
			if (way != waypoint) {
				final DefaultWeightedEdge newEdge = graph.addEdge(waypoint, way);
				if (newEdge != null) {
					graph.setEdgeWeight(newEdge, waypoint.distTo(way));
				}
			}
		}
	}

}
