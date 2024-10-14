package org.psu.spacetraders.api;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.psu.miningmanager.dto.SurveyResponse;
import org.psu.spacetraders.dto.DataWrapper;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

/**
 * Class to initiate surveys of resource sites
 */
@Dependent
@RegisterRestClient(configKey="spacetraders-api")
public interface SurveyClient {

	@POST
	@Path("/v2/my/ships/{shipId}/survey")
	public DataWrapper<SurveyResponse> survey(@PathParam("shipId") String shipId);
}
