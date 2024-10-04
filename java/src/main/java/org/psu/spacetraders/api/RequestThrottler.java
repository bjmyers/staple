package org.psu.spacetraders.api;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.psu.spacetraders.api.ThrottlerConfig.RateLimiterConfig;

import com.google.common.collect.EvictingQueue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Throttles requests to the space traders API based on the configured RateLimiters
 */
@JBossLog
@ApplicationScoped
public class RequestThrottler {

	private final List<RateLimiter> rateLimiters;

	@Inject
	public RequestThrottler(final ThrottlerConfig config) {
		this.rateLimiters = config.rateLimiters().stream().map(c -> new RateLimiter(c)).toList();
	}

	/**
	 * Note that this method will block until the rate limit has passed
	 *
	 * @param <T>            The type that is expected to be returned by the API
	 *                       interaction
	 * @param apiInteraction The action to perform that hits the API. This supplier
	 *                       should perform exactly one API call, this supplier will
	 *                       be invoked once the configured rate limit has passed
	 * @return The result of invoking the supplier.
	 */
	public <T> T throttle(Supplier<T> apiInteraction) {

		// The time when the api interaction can be performed.
		// If empty, the interaction can be performed immediately
		final Optional<Instant> whenToCall = rateLimiters.stream().map(RateLimiter::nextValidTime)
				.filter(Objects::nonNull).max(Instant::compareTo);

		// Sleep until the API is available again
		whenToCall.ifPresent(time -> {
			try {
				final Duration timeToSleep = Duration.between(Instant.now(), time);
				if (timeToSleep.compareTo(Duration.ofSeconds(1)) > 0) {
					log.infof("API Limit reached, next request can be processed in %s", timeToSleep);
				}
				Thread.sleep(timeToSleep);
			} catch (InterruptedException e) {
				log.warn("Request Throttler interrupted, you may see API requests fail");
				log.warn(e);
			}
		});

		final T response = apiInteraction.get();
		// Let the rate limiters know that another API call has been made
		rateLimiters.forEach(limiter -> limiter.requestSent(Instant.now()));

		return response;
	}

	/**
	 * Defines a message rate which cannot be surpassed
	 */
	private class RateLimiter {

		private final int maxMessages;
		private final Duration period;
		/**
		 * Holds the most recent message times, it only holds maxMessages elements.
		 */
		private final EvictingQueue<Instant> pastRequests;

		public RateLimiter(final RateLimiterConfig config) {
			this.maxMessages = config.requests();
			this.period = Duration.ofSeconds(config.period());
			this.pastRequests = EvictingQueue.create(maxMessages);
		}

		/**
		 * @return the next {@link Instant} where a request can be sent, sending a
		 *         request any earlier can result in space traders API failures. A null
		 *         return indicates that a message can be sent now
		 */
		public Instant nextValidTime() {
			if (pastRequests.size() < maxMessages) {
				// we have not yet sent enough messages to hit the max
				return null;
			}
			final Instant now = Instant.now();
			final Instant nextTime = pastRequests.peek().plus(period);
			if (nextTime.isBefore(now)) {
				// We've passed the limiting period
				return null;
			}
			return nextTime;
		}

		/**
		 * Updates the internal state of the object because a message was sent
		 * @param sentTime the time the message was sent
		 */
		public void requestSent(final Instant sentTime) {
			pastRequests.add(sentTime);
		}

	}

}
