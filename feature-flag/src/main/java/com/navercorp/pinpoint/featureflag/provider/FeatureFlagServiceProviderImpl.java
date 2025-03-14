package com.navercorp.pinpoint.featureflag.provider;

import com.navercorp.pinpoint.featureflag.config.FeatureFlagProperties;
import com.navercorp.pinpoint.featureflag.service.EnabledFeatureFlagService;
import com.navercorp.pinpoint.featureflag.service.FeatureFlagService;
import com.navercorp.pinpoint.featureflag.service.SimpleFeatureFlagService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class FeatureFlagServiceProviderImpl implements FeatureFlagServiceProvider {
    private static final Logger logger = LogManager.getLogger(FeatureFlagServiceProviderImpl.class);

    private final FeatureFlagProperties properties;

    public FeatureFlagServiceProviderImpl(FeatureFlagProperties properties) {
        this.properties = Objects.requireNonNull(properties);

        properties.forEach((feature, config) -> {
            logger.info("feature={}: enabled={}, enabledFor={}, disabledFor={}", feature, config.enabled(), config.enabledFor(), config.disabledFor());
            if (config.enabled() != null && (config.enabledFor() != null || config.disabledFor() != null)) {
                logger.warn("feature={}: enabled and application specific list is set at the same time", feature);
            }
            if (config.enabledFor() != null && config.disabledFor() != null) {
                logger.warn("feature={}: enabledFor and disabledFor are set at the same time", feature);
            }
        });
    }

    @Override
    public FeatureFlagService get(String featureName) {
        FeatureFlagProperties.Feature feature = properties.get(featureName);
        if (feature == null) {
            return new EnabledFeatureFlagService();
        }

        final boolean enabled;
        if (feature.enabled() == null) {
            // if there is no default
            // enabled when disabledFor is set and disabled when enabledFor is set
            // enabled when both are set or both are not set
            enabled = feature.disabledFor() != null || feature.enabledFor() == null;
        } else {
            enabled = feature.enabled();
        }

        return new SimpleFeatureFlagService(enabled, feature.enabledFor(), feature.disabledFor());
    }
}
