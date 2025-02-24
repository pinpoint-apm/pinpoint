package com.navercorp.pinpoint.featureflag.service;

import com.navercorp.pinpoint.featureflag.config.FeatureFlagProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class PropertyFeatureFlagService implements FeatureFlagService {
    private static final Logger logger = LogManager.getLogger(PropertyFeatureFlagService.class);

    private final FeatureFlagProperties properties;

    public PropertyFeatureFlagService(FeatureFlagProperties properties) {
        this.properties = Objects.requireNonNull(properties);

        properties.forEach((feature, config) -> {
            logger.info("features.{}: enabled={}, enabledFor={}, disabledFor={}", feature, config.enabled(), config.enabledFor(), config.disabledFor());
            if (config.enabled() != null && (config.enabledFor() != null || config.disabledFor() != null)) {
                logger.warn("features.{}: enabled and application specific list is set at the same time", feature);
            }
            if (config.enabledFor() != null && config.disabledFor() != null) {
                logger.warn("features.{}: enabledFor and disabledFor are set at the same time", feature);
            }
        });
    }

    @Override
    public boolean isEnabled(String featureName, String applicationName) {
        FeatureFlagProperties.Feature feature = properties.get(featureName);
        if (feature == null) {
            return true;
        }

        // application name is mentioned somewhere
        // prioritize disabledFor over enabledFor
        if (feature.disabledFor() != null && feature.disabledFor().contains(applicationName)) {
            return false;
        }
        if (feature.enabledFor() != null && feature.enabledFor().contains(applicationName)) {
            return true;
        }

        // application name is not mentioned anywhere
        if (feature.enabled() == null) {
            // if there is no default
            // enabled when disabledFor is set and disabled when enabledFor is set
            // enabled when both are set or both are not set
            return feature.disabledFor() != null || feature.enabledFor() == null;
        } else {
            return feature.enabled();
        }
    }
}
