package org.psu.init;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Startup
@Singleton
public class SystemBuilder {
	
	/**
	 * Constructor, this method queries the space traders API for system information
	 */
	@Inject
	public SystemBuilder() {
		System.out.println("Initializing the System");
	}

}
