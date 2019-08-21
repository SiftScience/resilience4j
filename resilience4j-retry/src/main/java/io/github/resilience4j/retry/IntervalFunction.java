package io.github.resilience4j.retry;

import static java.util.Objects.requireNonNull;
import java.time.Duration;
import java.util.function.Function;

import static io.github.resilience4j.retry.IntervalFunctionCompanion.checkAttempt;
import static io.github.resilience4j.retry.IntervalFunctionCompanion.checkInterval;
import static io.github.resilience4j.retry.IntervalFunctionCompanion.checkMultiplier;
import static io.github.resilience4j.retry.IntervalFunctionCompanion.checkRandomizationFactor;
import static io.github.resilience4j.retry.IntervalFunctionCompanion.randomize;
import io.vavr.collection.Stream;

/**
 * Dummy interface for backwards compatibility
 */
public interface IntervalFunction extends io.github.resilience4j.core.IntervalFunction {
    static io.github.resilience4j.core.IntervalFunction ofDefaults() {
        return of(DEFAULT_INITIAL_INTERVAL);
    }

    static io.github.resilience4j.core.IntervalFunction of(long intervalMillis, Function<Long, Long> backoffFunction) {
        checkInterval(intervalMillis);
        requireNonNull(backoffFunction);

        return (attempt) -> {
            checkAttempt(attempt);
            return Stream.iterate(intervalMillis, backoffFunction).get(attempt - 1);
        };
    }

    static io.github.resilience4j.core.IntervalFunction of(Duration interval, Function<Long, Long> backoffFunction) {
        return of(interval.toMillis(), backoffFunction);
    }

    static io.github.resilience4j.core.IntervalFunction of(long intervalMillis) {
        checkInterval(intervalMillis);
        return (attempt) -> {
            checkAttempt(attempt);
            return intervalMillis;
        };
    }

    static io.github.resilience4j.core.IntervalFunction of(Duration interval) {
        return of(interval.toMillis());
    }


    static io.github.resilience4j.core.IntervalFunction ofRandomized(long intervalMillis, double randomizationFactor) {
        checkInterval(intervalMillis);
        checkRandomizationFactor(randomizationFactor);
        return (attempt) -> {
            checkAttempt(attempt);
            return (long) randomize(intervalMillis, randomizationFactor);
        };
    }

    static io.github.resilience4j.core.IntervalFunction ofRandomized(Duration interval, double randomizationFactor) {
        return ofRandomized(interval.toMillis(), randomizationFactor);
    }

    static io.github.resilience4j.core.IntervalFunction ofRandomized(long interval) {
        return ofRandomized(interval, DEFAULT_RANDOMIZATION_FACTOR);
    }

    static io.github.resilience4j.core.IntervalFunction ofRandomized(Duration interval) {
        return ofRandomized(interval.toMillis(), DEFAULT_RANDOMIZATION_FACTOR);
    }

    static io.github.resilience4j.core.IntervalFunction ofRandomized() {
        return ofRandomized(DEFAULT_INITIAL_INTERVAL, DEFAULT_RANDOMIZATION_FACTOR);
    }

    static io.github.resilience4j.core.IntervalFunction ofExponentialBackoff(long initialIntervalMillis, double multiplier) {
        checkMultiplier(multiplier);
        return of(initialIntervalMillis, (x) -> (long) (x * multiplier));

    }

    static io.github.resilience4j.core.IntervalFunction ofExponentialBackoff(Duration initialInterval, double multiplier) {
        return ofExponentialBackoff(initialInterval.toMillis(), multiplier);
    }

    static io.github.resilience4j.core.IntervalFunction ofExponentialBackoff(long initialIntervalMillis) {
        return ofExponentialBackoff(initialIntervalMillis, DEFAULT_MULTIPLIER);
    }

    static io.github.resilience4j.core.IntervalFunction ofExponentialBackoff(Duration initialInterval) {
        return ofExponentialBackoff(initialInterval.toMillis(), DEFAULT_MULTIPLIER);
    }

    static io.github.resilience4j.core.IntervalFunction ofExponentialBackoff() {
        return ofExponentialBackoff(DEFAULT_INITIAL_INTERVAL, DEFAULT_MULTIPLIER);
    }

    static io.github.resilience4j.core.IntervalFunction ofExponentialRandomBackoff(
        long initialIntervalMillis,
        double multiplier,
        double randomizationFactor
    ) {
        checkInterval(initialIntervalMillis);
        checkMultiplier(multiplier);
        checkRandomizationFactor(randomizationFactor);
        return (attempt) -> {
            checkAttempt(attempt);
            final long interval = of(initialIntervalMillis, (x) -> (long) (x * multiplier)).apply(attempt);
            return (long) randomize(interval, randomizationFactor);
        };
    }

    static io.github.resilience4j.core.IntervalFunction ofExponentialRandomBackoff(
        Duration initialInterval,
        double multiplier,
        double randomizationFactor
    ) {
        return ofExponentialRandomBackoff(initialInterval.toMillis(), multiplier, randomizationFactor);
    }

    static io.github.resilience4j.core.IntervalFunction ofExponentialRandomBackoff(
        long initialIntervalMillis,
        double multiplier
    ) {
        return ofExponentialRandomBackoff(initialIntervalMillis, multiplier, DEFAULT_RANDOMIZATION_FACTOR);
    }

    static io.github.resilience4j.core.IntervalFunction ofExponentialRandomBackoff(
        Duration initialInterval,
        double multiplier
    ) {
        return ofExponentialRandomBackoff(initialInterval.toMillis(), multiplier, DEFAULT_RANDOMIZATION_FACTOR);
    }

    static io.github.resilience4j.core.IntervalFunction ofExponentialRandomBackoff(
        long initialIntervalMillis
    ) {
        return ofExponentialRandomBackoff(initialIntervalMillis, DEFAULT_MULTIPLIER);
    }

    static io.github.resilience4j.core.IntervalFunction ofExponentialRandomBackoff(
        Duration initialInterval
    ) {
        return ofExponentialRandomBackoff(initialInterval.toMillis(), DEFAULT_MULTIPLIER);
    }

    static io.github.resilience4j.core.IntervalFunction ofExponentialRandomBackoff() {
        return ofExponentialRandomBackoff(DEFAULT_INITIAL_INTERVAL, DEFAULT_MULTIPLIER, DEFAULT_RANDOMIZATION_FACTOR);
    }
}

final class IntervalFunctionCompanion {
    private IntervalFunctionCompanion() {
    }

    static double randomize(final double current, final double randomizationFactor) {
        final double delta = randomizationFactor * current;
        final double min = current - delta;
        final double max = current + delta;
        return (min + (Math.random() * (max - min + 1)));
    }

    static void checkInterval(long interval) {
        if (interval < 10) {
            throw new IllegalArgumentException("Illegal argument interval: " + interval + " millis");
        }
    }

    static void checkMultiplier(double multiplier) {
        if (multiplier < 1.0) {
            throw new IllegalArgumentException("Illegal argument multiplier: " + multiplier);
        }
    }

    static void checkRandomizationFactor(double randomizationFactor) {
        if (randomizationFactor < 0.0 || randomizationFactor >= 1.0) {
            throw new IllegalArgumentException("Illegal argument randomizationFactor: " + randomizationFactor);
        }
    }

    static void checkAttempt(long attempt) {
        if (attempt < 1) {
            throw new IllegalArgumentException("Illegal argument attempt: " + attempt);
        }
    }
}