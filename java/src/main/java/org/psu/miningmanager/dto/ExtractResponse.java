package org.psu.miningmanager.dto;

import org.psu.spacetraders.dto.Cargo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtractResponse {

	private Cooldown cooldown;
	private Cargo cargo;

}
