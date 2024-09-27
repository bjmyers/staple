package org.psu.testutils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.psu.spacetraders.api.RequestThrottler;

/**
 * Provides a mocked {@link RequestThrottler} which just invokes the api interaction
 */
public class TestRequestThrottler {

	public static RequestThrottler get() {
		final RequestThrottler throttler = mock(RequestThrottler.class);
		when(throttler.throttle(any())).thenAnswer(invocation -> {
			final Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
		});
		return throttler;
	}

}
