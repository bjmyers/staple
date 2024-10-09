package org.psu.init;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Startup Class
 */
@JBossLog
@QuarkusMain
public class Main {

    public static void main(String... args) {
        log.info("Beginning the STAPLE Application");
        Quarkus.run(StapleApplication.class, args);
    }

    public static class StapleApplication implements QuarkusApplication {

    	@Inject
    	private ShipLoader shipLoader;

		@Override
		public int run(String... args) throws Exception {
			// TODO Auto-generated method stub
			shipLoader.run();

			// Wait for termination signal
	        Quarkus.waitForExit();
			return 0;
		}

    }

}
