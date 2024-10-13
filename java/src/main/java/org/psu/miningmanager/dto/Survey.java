package org.psu.miningmanager.dto;

import java.time.Instant;
import java.util.List;

import org.psu.spacetraders.dto.Product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * The result of surveying a resource
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Survey {

	private String signature;
	private List<Product> deposits;
	private Instant expiration;

}
