package com.navercorp.pinpoint.featureflag.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleFeatureFlagServiceTest {
    static void assertEnabled(FeatureFlagService service, String application) {
        assertTrue(service.isEnabled(application));
    }

    static void assertDisabled(FeatureFlagService service, String application) {
        assertFalse(service.isEnabled(application));
    }

    @Test
    void global() {
        FeatureFlagService enabledFeature = new SimpleFeatureFlagService(true, null, null);
        FeatureFlagService disabledFeature = new SimpleFeatureFlagService(false, null, null);

        assertEnabled(enabledFeature, "other");
        assertDisabled(disabledFeature, "other");
    }

    @Test
    void enabledForApp() {
        FeatureFlagService enabledFeature = new SimpleFeatureFlagService(true, List.of("enabledApp"), null);
        FeatureFlagService disabledFeature = new SimpleFeatureFlagService(false, List.of("enabledApp"), null);

        assertEnabled(enabledFeature, "enabledApp");
        assertEnabled(disabledFeature, "enabledApp");

        assertEnabled(enabledFeature, "other");
        assertDisabled(disabledFeature, "other");
    }

    @Test
    void disabledForApp() {
        FeatureFlagService enabledFeature = new SimpleFeatureFlagService(true, null, List.of("disabledApp"));
        FeatureFlagService disabledFeature = new SimpleFeatureFlagService(false, null, List.of("disabledApp"));

        assertDisabled(enabledFeature, "disabledApp");
        assertDisabled(disabledFeature, "disabledApp");

        assertEnabled(enabledFeature, "other");
        assertDisabled(disabledFeature, "other");
    }

    @Test
    void bothSet() {
        FeatureFlagService enabledFeature = new SimpleFeatureFlagService(true, List.of("enabledApp"), List.of("disabledApp"));
        FeatureFlagService disabledFeature = new SimpleFeatureFlagService(false, List.of("enabledApp"), List.of("disabledApp"));

        assertEnabled(enabledFeature, "enabledApp");
        assertEnabled(disabledFeature, "enabledApp");

        assertDisabled(enabledFeature, "disabledApp");
        assertDisabled(disabledFeature, "disabledApp");

        assertEnabled(enabledFeature, "other");
        assertDisabled(disabledFeature, "other");
    }

    @Test
    void disabledIfClashed() {
        FeatureFlagService enabledFeature = new SimpleFeatureFlagService(true, List.of("app"), List.of("app"));
        assertDisabled(enabledFeature, "app");
    }
}