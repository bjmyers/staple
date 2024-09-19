package org.psu.init;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import lombok.extern.jbosslog.JBossLog;

/**
 * Startup Class
 */
@JBossLog
@QuarkusMain
public class Main {

    public static void main(String... args) {
        log.info("Beginning the STAPLE Application");
        Quarkus.run(args);
    }

}
