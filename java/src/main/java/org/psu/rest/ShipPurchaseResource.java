package org.psu.rest;

import org.psu.shiporchestrator.ShipJobQueue;
import org.psu.spacetraders.dto.ShipType;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.jbosslog.JBossLog;

/**
 * REST resource to indicate to the application that it should purchase a new
 * ship
 */
@Path("/purchase")
@JBossLog
@ApplicationScoped
public class ShipPurchaseResource {

	@Inject
	private ShipJobQueue shipJobQueue;

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public void purchaseShip(final String shipType) {
    	log.infof("Received Purchase Request with ship type %s", shipType);
    	final ShipType type = ShipType.valueOf(shipType);
    	shipJobQueue.setShipTypeToBuy(type);
    }
}
