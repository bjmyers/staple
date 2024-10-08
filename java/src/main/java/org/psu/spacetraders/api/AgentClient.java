package org.psu.spacetraders.api;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.psu.spacetraders.dto.Agent;
import org.psu.spacetraders.dto.DataWrapper;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 * Rest client to get a user's information
 */
@Dependent
@RegisterRestClient(configKey="spacetraders-api")
public interface AgentClient {

	/**
	 * @return a wrapped {@link Agent} object for the user
	 */
	@GET
	@Path("/v2/my/agent")
	public DataWrapper<Agent> getAgent();

}
