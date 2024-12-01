package org.psu;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.jbosslog.JBossLog;

/**
 * Basic resource to return a hello world response
 */
@Path("/hello")
@JBossLog
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
    	log.info("Hit the Hello Endpoint");
        return "Hello from the STAPLE Application";
    }
}
