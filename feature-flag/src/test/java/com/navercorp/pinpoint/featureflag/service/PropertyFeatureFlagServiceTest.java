package com.navercorp.pinpoint.featureflag.service;

import com.navercorp.pinpoint.featureflag.config.FeatureFlagProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PropertyFeatureFlagServiceTest {
    static final String FEATURE_NAME = "feature";

    @Test
    void notDefined() {
        PropertyFeatureFlagService service = new PropertyFeatureFlagService(new FeatureFlagProperties());
        assertTrue(service.isEnabled("nonExistent", "app"));
    }

    @Test
    void global() {
        PropertyFeatureFlagService defaultFeature = buildService(null, null, null);
        PropertyFeatureFlagService enabledFeature = buildService(true, null, null);
        PropertyFeatureFlagService disabledFeature = buildService(false, null, null);

        assertEnabled(defaultFeature, "app");
        assertEnabled(enabledFeature, "app");
        assertDisabled(disabledFeature, "app");
    }

    @Test
    void enabledForApp() {
        PropertyFeatureFlagService defaultFeature = buildService(null, List.of("enabledApp"), null);
        PropertyFeatureFlagService enabledFeature = buildService(true, List.of("enabledApp"), null);
        PropertyFeatureFlagService disabledFeature = buildService(false, List.of("enabledApp"), null);

        assertEnabled(defaultFeature, "enabledApp");
        assertEnabled(enabledFeature, "enabledApp");
        assertEnabled(disabledFeature, "enabledApp");

        assertDisabled(defaultFeature, "other");
        assertEnabled(enabledFeature, "other");
        assertDisabled(disabledFeature, "other");
    }

    @Test
    void disabledForApp() {
        PropertyFeatureFlagService defaultFeature = buildService(null, null, List.of("disabledApp"));
        PropertyFeatureFlagService enabledFeature = buildService(true, null, List.of("disabledApp"));
        PropertyFeatureFlagService disabledFeature = buildService(false, null, List.of("disabledApp"));

        assertDisabled(defaultFeature, "disabledApp");
        assertDisabled(enabledFeature, "disabledApp");
        assertDisabled(disabledFeature, "disabledApp");

        assertEnabled(defaultFeature, "other");
        assertEnabled(enabledFeature, "other");
        assertDisabled(disabledFeature, "other");
    }

    @Test
    void bothSet() {
        PropertyFeatureFlagService defaultFeature = buildService(null, List.of("enabledApp"), List.of("disabledApp"));
        PropertyFeatureFlagService enabledFeature = buildService(true, List.of("enabledApp"), List.of("disabledApp"));
        PropertyFeatureFlagService disabledFeature = buildService(false, List.of("enabledApp"), List.of("disabledApp"));

        assertEnabled(defaultFeature, "enabledApp");
        assertEnabled(enabledFeature, "enabledApp");
        assertEnabled(disabledFeature, "enabledApp");

        assertDisabled(defaultFeature, "disabledApp");
        assertDisabled(enabledFeature, "disabledApp");
        assertDisabled(disabledFeature, "disabledApp");

        assertEnabled(defaultFeature, "other");
        assertEnabled(enabledFeature, "other");
        assertDisabled(disabledFeature, "other");
    }

    @Test
    void disabledIfClashed() {
        PropertyFeatureFlagService enabledFeature = buildService(true, List.of("app"), List.of("app"));
        assertDisabled(enabledFeature, "app");
    }

    static PropertyFeatureFlagService buildService(Boolean enabled, List<String> enabledFor, List<String> disabledFor) {
        FeatureFlagProperties properties = new FeatureFlagProperties();
        properties.put(FEATURE_NAME, new FeatureFlagProperties.Feature(enabled, enabledFor, disabledFor));

        return new PropertyFeatureFlagService(properties);
    }

    static void assertEnabled(PropertyFeatureFlagService service, String application) {
        assertTrue(service.isEnabled(FEATURE_NAME, application));
    }

    static void assertDisabled(PropertyFeatureFlagService service, String application) {
        assertFalse(service.isEnabled(FEATURE_NAME, application));
    }
}