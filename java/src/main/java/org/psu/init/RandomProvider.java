package org.psu.init;

import java.util.Random;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Provides randomness, useful for mocking random behavior in unit tests
 */
@ApplicationScoped
public class RandomProvider {

	private Random random;

	@Inject
	public RandomProvider() {
		random = new Random(0);
	}

	/**
	 * @return a random double between 0.0 and 1.0
	 */
	public double nextDouble() {
		return this.random.nextDouble();
	}

}
