package org.psu.spacetraders.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * The request for a ship to navigate to a waypoint
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class NavigationRequest {

	private String waypointSymbol;

}
