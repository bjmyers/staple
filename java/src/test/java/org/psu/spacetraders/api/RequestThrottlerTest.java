package org.psu.spacetraders.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.psu.spacetraders.api.ThrottlerConfig.RateLimiterConfig;

/**
 * Tests for {@link RequestThrottler}
 */
public class RequestThrottlerTest {

	/**
	 * Tests {@link RequestThrottler#throttle}
	 */
	@Test
	public void throttle() {

		final RateLimiterConfig limiterConfig = mock(RateLimiterConfig.class);
		when(limiterConfig.requests()).thenReturn(10);
		when(limiterConfig.period()).thenReturn(1);

		final ThrottlerConfig config = mock(ThrottlerConfig.class);
		when(config.rateLimiters()).thenReturn(List.of(limiterConfig));

		final RequestThrottler throttler = new RequestThrottler(config);

		for (int i = 0; i < 15; i++) {
			// Throttle a bunch of things
			throttler.throttle(() -> 1);
		}
	}

}
