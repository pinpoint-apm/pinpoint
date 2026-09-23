/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.featureflag.service.properties;

import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.featureflag.config.FeatureFlagProperties;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureFlagServiceFactoryTest {
    private static final String DEFAULT_SERVICE = Service.DEFAULT.getServiceName();

    static FeatureFlagService buildService(Boolean enabled, List<FeatureFlagTarget> enabledFor, List<FeatureFlagTarget> disabledFor) {
        FeatureFlagProperties properties = new FeatureFlagProperties();
        properties.add(new FeatureFlagProperties.FeatureSpec("feature", enabled, enabledFor, disabledFor));
        FeatureFlagServiceFactory factory = new FeatureFlagServiceFactory(properties);
        return factory.get("feature");
    }

    static void assertEnabled(FeatureFlagService service, String application) {
        assertTrue(service.isEnabled(DEFAULT_SERVICE, application));
    }

    static void assertDisabled(FeatureFlagService service, String application) {
        assertFalse(service.isEnabled(DEFAULT_SERVICE, application));
    }

    @Test
    void factoryFiltersInvalidTargetsWithoutChangingConfigurationData() {
        FeatureFlagTarget invalid = new FeatureFlagTarget(" ", "app");
        FeatureFlagTarget valid = new FeatureFlagTarget("service-a", "app");
        List<FeatureFlagTarget> expectedTargets = List.of(invalid, valid);
        List<FeatureFlagTarget> targets = new ArrayList<>(expectedTargets);
        FeatureFlagProperties.FeatureSpec spec = new FeatureFlagProperties.FeatureSpec("feature", null, targets, null);
        assertEquals(expectedTargets, spec.enabledFor());

        FeatureFlagProperties properties = new FeatureFlagProperties();
        properties.add(spec);
        FeatureFlagService service = new FeatureFlagServiceFactory(properties).get("feature");
        assertTrue(service.isEnabled("service-a", "app"));
        assertFalse(service.isEnabled("other", "app"));
        assertFalse(service.isEnabled(" ", "app"));
        assertEquals(expectedTargets, spec.enabledFor());
    }

    @Test
    void factoryFiltersInvalidDisabledTargetsWithoutChangingConfigurationData() {
        FeatureFlagTarget invalid = new FeatureFlagTarget(" ", "app");
        FeatureFlagTarget valid = new FeatureFlagTarget("service-a", "app");
        List<FeatureFlagTarget> expectedTargets = List.of(invalid, valid);
        List<FeatureFlagTarget> targets = new ArrayList<>(expectedTargets);
        FeatureFlagProperties.FeatureSpec spec = new FeatureFlagProperties.FeatureSpec("feature", null, null, targets);
        FeatureFlagProperties properties = new FeatureFlagProperties();
        properties.add(spec);

        FeatureFlagService service = new FeatureFlagServiceFactory(properties).get("feature");

        assertTrue(service.isDisabled("service-a", "app"));
        assertTrue(service.isEnabled("other", "app"));
        assertTrue(service.isEnabled(" ", "app"));
        assertEquals(expectedTargets, spec.disabledFor());
    }

    @Test
    void invalidTargetsDoNotAffectDefaultRules() {
        List<FeatureFlagTarget> invalidTargets = Arrays.asList(
                null,
                new FeatureFlagTarget(null, "app"),
                new FeatureFlagTarget(" ", "app"),
                new FeatureFlagTarget("service-a", null),
                new FeatureFlagTarget("service-a", " "));
        for (FeatureFlagTarget invalid : invalidTargets) {
            List<FeatureFlagTarget> targets = Collections.singletonList(invalid);
            FeatureFlagService enabledListSet = buildService(null, targets, null);
            FeatureFlagService bothSet = buildService(null,
                    List.of(new FeatureFlagTarget("service-a", "app")), targets);

            assertTrue(enabledListSet.isEnabled("service-a", "other"), "Invalid enabled target: " + invalid);
            assertTrue(bothSet.isEnabled("service-a", "app"), "Invalid disabled target: " + invalid);
            assertFalse(bothSet.isEnabled("service-a", "other"), "Invalid disabled target: " + invalid);
        }
    }

    @Test
    void notDefined() {
        FeatureFlagServiceFactory factory = new FeatureFlagServiceFactory(new FeatureFlagProperties());
        FeatureFlagService service = factory.get("nonExistent");
        assertTrue(service.isEnabled(DEFAULT_SERVICE, "app"));
        assertTrue(service.isEnabled("service-a", "app"));
        assertFalse(service.isDisabled("service-a", "app"));
    }

    @Test
    void serviceEntriesPreserveDefaultRules() {
        FeatureFlagService noneSet = buildService(null, null, null);
        FeatureFlagService enabledListSet = buildService(null, List.of(new FeatureFlagTarget("service-a", "app")), null);
        FeatureFlagService disabledListSet = buildService(null, null, List.of(new FeatureFlagTarget("service-a", "app")));
        FeatureFlagService bothSet = buildService(null, List.of(new FeatureFlagTarget("service-a", "app")), List.of(new FeatureFlagTarget("service-b", "app")));

        assertTrue(noneSet.isEnabled("service-a", "other"));
        assertTrue(enabledListSet.isEnabled("service-a", "app"));
        assertFalse(enabledListSet.isEnabled("service-b", "app"));
        assertTrue(disabledListSet.isDisabled("service-a", "app"));
        assertTrue(disabledListSet.isEnabled("service-b", "app"));
        assertTrue(bothSet.isEnabled("service-a", "app"));
        assertTrue(bothSet.isDisabled("service-b", "app"));
        assertTrue(bothSet.isEnabled("service-c", "app"));
    }

    @Test
    void defaults() {
        FeatureFlagService noneSet = buildService(null, null, null);
        FeatureFlagService enabledListSet = buildService(null, List.of(new FeatureFlagTarget("DEFAULT", "enabledApp")), null);
        FeatureFlagService disabledListSet = buildService(null, null, List.of(new FeatureFlagTarget("DEFAULT", "disabledApp")));
        FeatureFlagService bothSet = buildService(null, List.of(new FeatureFlagTarget("DEFAULT", "enabledApp")), List.of(new FeatureFlagTarget("DEFAULT", "disabledApp")));

        assertEnabled(noneSet, "other");
        assertDisabled(enabledListSet, "other");
        assertEnabled(disabledListSet, "other");
        assertEnabled(bothSet, "other");
    }
}