package org.psu.spacetraders.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipRoute {

	private RoutePoint origin;
	private RoutePoint destination;
	private String departureTime;
	private String arrival;


	@Data
	@RequiredArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class RoutePoint {

		private String symbol;
		private int x;
		private int y;

	}

}