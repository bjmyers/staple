package org.psu.testdriver.client;

import org.psu.spacetraders.api.NavigationClient;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.DockResponse;
import org.psu.spacetraders.dto.NavigationRequest;
import org.psu.spacetraders.dto.NavigationResponse;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Local version of the {@link NavigationClient}
 */
@ApplicationScoped
public class LocalNavigationClient implements NavigationClient {

	@Override
	public DataWrapper<DockResponse> orbit(String shipId) {
		return null;
	}

	@Override
	public DataWrapper<DockResponse> dock(String shipId) {
		return null;
	}

	@Override
	public DataWrapper<NavigationResponse> navigate(String shipId, NavigationRequest navRequest) {
		return null;
	}

}
