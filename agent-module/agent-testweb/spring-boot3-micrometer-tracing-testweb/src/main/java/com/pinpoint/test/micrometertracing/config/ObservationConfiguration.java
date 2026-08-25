package com.pinpoint.test.micrometertracing.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enables {@code @Observed}. These spans exist only in the Observation-based SDK path,
 * which is why the OTel bridge cannot simply be replaced by the OpenTelemetry Java Agent.
 */
@Configuration(proxyBeanMethods = false)
public class ObservationConfiguration {

    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}
