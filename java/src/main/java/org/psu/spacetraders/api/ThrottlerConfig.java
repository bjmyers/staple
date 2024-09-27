package org.psu.spacetraders.api;

import java.util.List;

import io.smallrye.config.ConfigMapping;

/**
 * The configuration for a {@link RequestThrottler}
 */
@ConfigMapping(prefix = "app.throttler")
public interface ThrottlerConfig {

	List<RateLimiterConfig> rateLimiters();

	/**
	 * Defines a given request rate limit. For example, no more than 30 requests can
	 * be sent per 60 second period
	 */
	interface RateLimiterConfig {
		// The number of requests which can be sent
		int requests();
		// The period in seconds in which that number of requests can be sent
		int period();
	}

}
