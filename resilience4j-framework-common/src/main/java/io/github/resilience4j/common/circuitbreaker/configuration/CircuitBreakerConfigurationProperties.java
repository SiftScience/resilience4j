/*
 * Copyright 2019 Dan Maas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.resilience4j.common.circuitbreaker.configuration;


import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.Builder;
import io.github.resilience4j.common.utils.ConfigUtils;
import io.github.resilience4j.core.ClassUtils;
import io.github.resilience4j.core.ConfigurationNotFoundException;
import io.github.resilience4j.core.StringUtils;
import io.github.resilience4j.core.lang.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.*;

public class CircuitBreakerConfigurationProperties {

	private Map<String, InstanceProperties> instances = new HashMap<>();
	private Map<String, InstanceProperties> configs = new HashMap<>();

	public Optional<InstanceProperties> findCircuitBreakerProperties(String name) {
		InstanceProperties instanceProperties = instances.get(name);
		if(instanceProperties == null){
			instanceProperties = configs.get("default");
		}
		return Optional.ofNullable(instanceProperties);
	}

	public CircuitBreakerConfig createCircuitBreakerConfig(InstanceProperties instanceProperties) {
		if (StringUtils.isNotEmpty(instanceProperties.getBaseConfig())) {
			InstanceProperties baseProperties = configs.get(instanceProperties.getBaseConfig());
			if (baseProperties == null) {
				throw new ConfigurationNotFoundException(instanceProperties.getBaseConfig());
			}
			return buildConfigFromBaseConfig(instanceProperties, baseProperties);
		}
		return buildConfig(custom(), instanceProperties);
	}

	private CircuitBreakerConfig buildConfigFromBaseConfig(InstanceProperties instanceProperties, InstanceProperties baseProperties) {
		ConfigUtils.mergePropertiesIfAny(instanceProperties, baseProperties);
		CircuitBreakerConfig baseConfig = buildConfig(custom(), baseProperties);
		return buildConfig(from(baseConfig), instanceProperties);
	}

	@SuppressWarnings("deprecation") // deprecated API use left for backward compatibility
	private CircuitBreakerConfig buildConfig(Builder builder, InstanceProperties properties) {
		if (properties == null) {
			return builder.build();
		}
		if (properties.getWaitDurationInOpenState() != null) {
			builder.waitDurationInOpenState(properties.getWaitDurationInOpenState());
		}

		if (properties.getFailureRateThreshold() != null) {
			builder.failureRateThreshold(properties.getFailureRateThreshold());
		}

		if (properties.getWritableStackTraceEnabled() != null) {
			builder.writableStackTraceEnabled(properties.getWritableStackTraceEnabled());
		}

		if (properties.getSlowCallRateThreshold() != null) {
			builder.slowCallRateThreshold(properties.getSlowCallRateThreshold());
		}

		if (properties.getSlowCallDurationThreshold() != null) {
			builder.slowCallDurationThreshold(properties.getSlowCallDurationThreshold());
		}

		if (properties.getRingBufferSizeInClosedState() != null) {
			builder.ringBufferSizeInClosedState(properties.getRingBufferSizeInClosedState());
		}

		if (properties.getSlidingWindowSize() != null) {
			builder.slidingWindowSize(properties.getSlidingWindowSize());
		}

		if (properties.getMinimumNumberOfCalls() != null) {
			builder.minimumNumberOfCalls(properties.getMinimumNumberOfCalls());
		}

		if (properties.getSlidingWindowType() != null) {
			builder.slidingWindowType(properties.getSlidingWindowType());
		}

		if (properties.getRingBufferSizeInHalfOpenState() != null) {
			builder.ringBufferSizeInHalfOpenState(properties.getRingBufferSizeInHalfOpenState());
		}

		if (properties.getPermittedNumberOfCallsInHalfOpenState() != null) {
			builder.permittedNumberOfCallsInHalfOpenState(properties.getPermittedNumberOfCallsInHalfOpenState());
		}

		if (properties.recordFailurePredicate != null) {
			buildRecordFailurePredicate(properties, builder);
		}

		if (properties.recordExceptions != null) {
			builder.recordExceptions(properties.recordExceptions);
		}

		if (properties.ignoreExceptions != null) {
			builder.ignoreExceptions(properties.ignoreExceptions);
		}

		if (properties.automaticTransitionFromOpenToHalfOpenEnabled != null) {
			builder.automaticTransitionFromOpenToHalfOpenEnabled(properties.automaticTransitionFromOpenToHalfOpenEnabled);
		}

		return builder.build();
	}

	private void buildRecordFailurePredicate(InstanceProperties properties, Builder builder) {
		if (properties.getRecordFailurePredicate() != null) {
			Predicate<Throwable> predicate = ClassUtils.instantiatePredicateClass(properties.getRecordFailurePredicate());
			if (predicate != null) {
				builder.recordException(predicate);
			}
		}
	}

	@Nullable
	public InstanceProperties getBackendProperties(String backend) {
		return instances.get(backend);
	}

	public Map<String, InstanceProperties> getInstances() {
		return instances;
	}

	/**
	 * For backwards compatibility when setting backends in configuration properties.
	 */
	public Map<String, InstanceProperties> getBackends() {
		return instances;
	}

	public Map<String, InstanceProperties> getConfigs() {
		return configs;
	}

	/**
	 * Class storing property values for configuring {@link io.github.resilience4j.circuitbreaker.CircuitBreaker} instances.
	 */
	public static class InstanceProperties {

		@Nullable
		private Duration waitDurationInOpenState;

		@Nullable
		private Duration slowCallDurationThreshold;

		@Nullable
		private Float failureRateThreshold;

		@Nullable
		private Float slowCallRateThreshold;

		@Nullable
		@Deprecated
		@SuppressWarnings("DeprecatedIsStillUsed") // Left for backward compatibility
		private Integer ringBufferSizeInClosedState;

		@Nullable
		private SlidingWindowType slidingWindowType;

		@Nullable
		private Integer slidingWindowSize;

		@Nullable
		private Integer minimumNumberOfCalls;

		@Nullable
		private Integer permittedNumberOfCallsInHalfOpenState;

		@Nullable
		@Deprecated
		@SuppressWarnings("DeprecatedIsStillUsed") // Left for backward compatibility
		private Integer ringBufferSizeInHalfOpenState;

		@Nullable
		private Boolean automaticTransitionFromOpenToHalfOpenEnabled;

		@Nullable
		private Boolean writableStackTraceEnabled;

		@Nullable
		private Integer eventConsumerBufferSize;

		@Nullable
		private Boolean registerHealthIndicator;

		@Nullable
		private Class<Predicate<Throwable>> recordFailurePredicate;

		@Nullable
		private Class<? extends Throwable>[] recordExceptions;

		@Nullable
		private Class<? extends Throwable>[] ignoreExceptions;

		@Nullable
		private String baseConfig;


		/**
		 * Returns the failure rate threshold for the circuit breaker as percentage.
		 *
		 * @return the failure rate threshold
		 */
		@Nullable
		public Float getFailureRateThreshold() {
			return failureRateThreshold;
		}

		/**
		 * Sets the failure rate threshold for the circuit breaker as percentage.
		 *
		 * @param failureRateThreshold the failure rate threshold
		 */
		public InstanceProperties setFailureRateThreshold(Float failureRateThreshold) {
            Objects.requireNonNull(failureRateThreshold);
            if (failureRateThreshold < 1 || failureRateThreshold > 100) {
                throw new IllegalArgumentException("failureRateThreshold must be between 1 and 100.");
            }

			this.failureRateThreshold = failureRateThreshold;
			return this;
		}

		/**
		 * Returns the wait duration the CircuitBreaker will stay open, before it switches to half closed.
		 *
		 * @return the wait duration
		 */
		@Nullable
		public Duration getWaitDurationInOpenState() {
			return waitDurationInOpenState;
		}

		/**
		 * Sets the wait duration the CircuitBreaker should stay open, before it switches to half closed.
		 *
		 * @param waitDurationInOpenStateMillis the wait duration
		 */
		public InstanceProperties setWaitDurationInOpenState(Duration waitDurationInOpenStateMillis) {
            Objects.requireNonNull(waitDurationInOpenStateMillis);
            if (waitDurationInOpenStateMillis.toMillis() < 1) {
                throw new IllegalArgumentException(
                        "waitDurationInOpenStateMillis must be greater than or equal to 1 millis.");
            }

            this.waitDurationInOpenState = waitDurationInOpenStateMillis;
			return this;
		}

		/**
		 * Returns the ring buffer size for the circuit breaker while in closed state.
		 *
		 * @return the ring buffer size
		 */
		@Nullable
		public Integer getRingBufferSizeInClosedState() {
			return ringBufferSizeInClosedState;
		}

		/**
		 * Sets the ring buffer size for the circuit breaker while in closed state.
		 *
		 * @param ringBufferSizeInClosedState the ring buffer size
		 * @deprecated Use {@link #setSlidingWindowSize(Integer)} instead.
		 */
		@Deprecated
		public InstanceProperties setRingBufferSizeInClosedState(Integer ringBufferSizeInClosedState) {
            Objects.requireNonNull(ringBufferSizeInClosedState);
            if (ringBufferSizeInClosedState < 1) {
                throw new IllegalArgumentException("ringBufferSizeInClosedState must be greater than or equal to 1.");
            }

            this.ringBufferSizeInClosedState = ringBufferSizeInClosedState;
			return this;
		}

		/**
		 * Returns the ring buffer size for the circuit breaker while in half open state.
		 *
		 * @return the ring buffer size
		 */
		@Nullable
		public Integer getRingBufferSizeInHalfOpenState() {
			return ringBufferSizeInHalfOpenState;
		}

		/**
		 * Sets the ring buffer size for the circuit breaker while in half open state.
		 *
		 * @param ringBufferSizeInHalfOpenState the ring buffer size
		 * @deprecated Use {@link #setPermittedNumberOfCallsInHalfOpenState(Integer)} instead.
		 */
		@Deprecated
		public InstanceProperties setRingBufferSizeInHalfOpenState(Integer ringBufferSizeInHalfOpenState) {
            Objects.requireNonNull(ringBufferSizeInHalfOpenState);
            if (ringBufferSizeInHalfOpenState < 1) {
                throw new IllegalArgumentException("ringBufferSizeInHalfOpenState must be greater than or equal to 1.");
            }

            this.ringBufferSizeInHalfOpenState = ringBufferSizeInHalfOpenState;
			return this;
		}

		/**
		 * Returns if we should automatically transition to half open after the timer has run out.
		 *
		 * @return setAutomaticTransitionFromOpenToHalfOpenEnabled if we should automatically go to half open or not
		 */
		public Boolean getAutomaticTransitionFromOpenToHalfOpenEnabled() {
			return this.automaticTransitionFromOpenToHalfOpenEnabled;
		}

		/**
		 * Sets if we should automatically transition to half open after the timer has run out.
		 *
		 * @param automaticTransitionFromOpenToHalfOpenEnabled The flag for automatic transition to half open after the timer has run out.
		 */
		public InstanceProperties setAutomaticTransitionFromOpenToHalfOpenEnabled(Boolean automaticTransitionFromOpenToHalfOpenEnabled) {
			this.automaticTransitionFromOpenToHalfOpenEnabled = automaticTransitionFromOpenToHalfOpenEnabled;
			return this;
		}

		/**
		 * Returns if we should enable writable stack traces or not.
		 *
		 * @return writableStackTraceEnabled if we should enable writable stack traces or not.
		 */
        @Nullable
        public Boolean getWritableStackTraceEnabled() {
			return this.writableStackTraceEnabled;
		}

		/**
		 * Sets if we should enable writable stack traces or not.
		 *
		 * @param writableStackTraceEnabled The flag to enable writable stack traces.
		 */
		public InstanceProperties setWritableStackTraceEnabled(Boolean writableStackTraceEnabled) {
			this.writableStackTraceEnabled = writableStackTraceEnabled;
			return this;
		}

        @Nullable
        public Integer getEventConsumerBufferSize() {
			return eventConsumerBufferSize;
		}

		public InstanceProperties setEventConsumerBufferSize(Integer eventConsumerBufferSize) {
            Objects.requireNonNull(eventConsumerBufferSize);
            if (eventConsumerBufferSize < 1) {
                throw new IllegalArgumentException("eventConsumerBufferSize must be greater than or equal to 1.");
            }

            this.eventConsumerBufferSize = eventConsumerBufferSize;
			return this;
		}

        @Nullable
        public Boolean getRegisterHealthIndicator() {
			return registerHealthIndicator;
		}

		public InstanceProperties setRegisterHealthIndicator(Boolean registerHealthIndicator) {
			this.registerHealthIndicator = registerHealthIndicator;
			return this;
		}

		@Nullable
		public Class<Predicate<Throwable>> getRecordFailurePredicate() {
			return recordFailurePredicate;
		}

		public InstanceProperties setRecordFailurePredicate(Class<Predicate<Throwable>> recordFailurePredicate) {
			this.recordFailurePredicate = recordFailurePredicate;
			return this;
		}

		@Nullable
		public Class<? extends Throwable>[] getRecordExceptions() {
			return recordExceptions;
		}

		public InstanceProperties setRecordExceptions(Class<? extends Throwable>[] recordExceptions) {
			this.recordExceptions = recordExceptions;
			return this;
		}

		@Nullable
		public Class<? extends Throwable>[] getIgnoreExceptions() {
			return ignoreExceptions;
		}

		public InstanceProperties setIgnoreExceptions(Class<? extends Throwable>[] ignoreExceptions) {
			this.ignoreExceptions = ignoreExceptions;
			return this;
		}

		/**
		 * Gets the shared configuration name. If this is set, the configuration builder will use the the shared
		 * configuration backend over this one.
		 *
		 * @return The shared configuration name.
		 */
		@Nullable
		public String getBaseConfig() {
			return baseConfig;
		}

		/**
		 * Sets the shared configuration name. If this is set, the configuration builder will use the the shared
		 * configuration backend over this one.
		 *
		 * @param baseConfig The shared configuration name.
		 */
		public InstanceProperties setBaseConfig(String baseConfig) {
			this.baseConfig = baseConfig;
			return this;
		}

		@Nullable
		public Integer getPermittedNumberOfCallsInHalfOpenState() {
			return permittedNumberOfCallsInHalfOpenState;
		}

		public void setPermittedNumberOfCallsInHalfOpenState(Integer permittedNumberOfCallsInHalfOpenState) {
            Objects.requireNonNull(permittedNumberOfCallsInHalfOpenState);
            if (permittedNumberOfCallsInHalfOpenState < 1) {
                throw new IllegalArgumentException(
                        "permittedNumberOfCallsInHalfOpenState must be greater than or equal to 1.");
            }

            this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
		}

		@Nullable
		public Integer getMinimumNumberOfCalls() {
			return minimumNumberOfCalls;
		}

		public void setMinimumNumberOfCalls(Integer minimumNumberOfCalls) {
            Objects.requireNonNull(minimumNumberOfCalls);
            if (minimumNumberOfCalls < 1) {
                throw new IllegalArgumentException("minimumNumberOfCalls must be greater than or equal to 1.");
            }

            this.minimumNumberOfCalls = minimumNumberOfCalls;
		}

		@Nullable
		public Integer getSlidingWindowSize() {
			return slidingWindowSize;
		}

		public void setSlidingWindowSize(Integer slidingWindowSize) {
            Objects.requireNonNull(slidingWindowSize);
            if (slidingWindowSize < 1) {
                throw new IllegalArgumentException("slidingWindowSize must be greater than or equal to 1.");
            }

            this.slidingWindowSize = slidingWindowSize;
		}

		@Nullable
		public Float getSlowCallRateThreshold() {
			return slowCallRateThreshold;
		}

		public void setSlowCallRateThreshold(Float slowCallRateThreshold) {
            Objects.requireNonNull(slowCallRateThreshold);
            if (slowCallRateThreshold < 1 || slowCallRateThreshold > 100) {
                throw new IllegalArgumentException("slowCallRateThreshold must be between 1 and 100.");
            }

            this.slowCallRateThreshold = slowCallRateThreshold;
		}

		@Nullable
		public Duration getSlowCallDurationThreshold() {
			return slowCallDurationThreshold;
		}

		public void setSlowCallDurationThreshold(Duration slowCallDurationThreshold) {
            Objects.requireNonNull(slowCallDurationThreshold);
            if (slowCallDurationThreshold.toNanos() < 1) {
                throw new IllegalArgumentException(
                        "waitDurationInOpenStateMillis must be greater than or equal to 1 nanos.");
            }

            this.slowCallDurationThreshold = slowCallDurationThreshold;
		}

		@Nullable
		public SlidingWindowType getSlidingWindowType() {
			return slidingWindowType;
		}

		public void setSlidingWindowType(SlidingWindowType slidingWindowType) {
			this.slidingWindowType = slidingWindowType;
		}
	}

}
