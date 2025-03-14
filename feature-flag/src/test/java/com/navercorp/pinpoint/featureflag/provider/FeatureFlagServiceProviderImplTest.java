package com.navercorp.pinpoint.featureflag.provider;

import com.navercorp.pinpoint.featureflag.config.FeatureFlagProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureFlagServiceProviderImplTest {
    static final String FEATURE_NAME = "feature";

    static FeatureFlagServiceProviderImpl buildProvider(Boolean enabled, List<String> enabledFor, List<String> disabledFor) {
        FeatureFlagProperties properties = new FeatureFlagProperties();
        properties.put(FEATURE_NAME, new FeatureFlagProperties.Feature(enabled, enabledFor, disabledFor));
        return new FeatureFlagServiceProviderImpl(properties);
    }

    @Test
    void defaultEnabled() {
        FeatureFlagServiceProviderImpl sut = buildProvider(null, null, null);

        assertTrue(sut.get(FEATURE_NAME).isEnabled("app"));
        assertTrue(sut.get("other").isEnabled("app"));
    }

    @Test
    void enabledFor() {
        FeatureFlagServiceProviderImpl sut = buildProvider(null, List.of("app"), null);

        assertTrue(sut.get(FEATURE_NAME).isEnabled("app"));
        assertFalse(sut.get(FEATURE_NAME).isEnabled("other"));
    }
}