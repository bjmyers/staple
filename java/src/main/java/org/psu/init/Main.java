package org.psu.init;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Startup Class
 */
@QuarkusMain
public class Main {
	
    public static void main(String... args) {
        System.out.println("Beginning the STAPLE Application");
        Quarkus.run(args); 
    }

}
