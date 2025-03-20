package com.navercorp.pinpoint.featureflag.service;

import com.navercorp.pinpoint.featureflag.config.FeatureFlagProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureFlagServiceFactoryTest {
    static FeatureFlagService buildService(Boolean enabled, List<String> enabledFor, List<String> disabledFor) {
        FeatureFlagProperties properties = new FeatureFlagProperties();
        properties.add(new FeatureFlagProperties.FeatureSpec("feature", enabled, enabledFor, disabledFor));
        FeatureFlagServiceFactory factory = new FeatureFlagServiceFactory(properties);
        return factory.get("feature");
    }

    static void assertEnabled(FeatureFlagService service, String application) {
        assertTrue(service.isEnabled(application));
    }

    static void assertDisabled(FeatureFlagService service, String application) {
        assertFalse(service.isEnabled(application));
    }

    @Test
    void notDefined() {
        FeatureFlagServiceFactory factory = new FeatureFlagServiceFactory(new FeatureFlagProperties());
        FeatureFlagService service = factory.get("nonExistent");
        assertTrue(service.isEnabled("app"));
    }

    @Test
    void defaults() {
        FeatureFlagService noneSet = buildService(null, null, null);
        FeatureFlagService enabledListSet = buildService(null, List.of("enabledApp"), null);
        FeatureFlagService disabledListSet = buildService(null, null, List.of("disabledApp"));
        FeatureFlagService bothSet = buildService(null, List.of("enabledApp"), List.of("disabledApp"));

        assertEnabled(noneSet, "other");
        assertDisabled(enabledListSet, "other");
        assertEnabled(disabledListSet, "other");
        assertEnabled(bothSet, "other");
    }
}