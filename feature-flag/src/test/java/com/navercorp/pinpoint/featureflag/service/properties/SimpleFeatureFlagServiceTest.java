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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleFeatureFlagServiceTest {
    @Test
    void global() {
        FeatureFlagService enabled = new SimpleFeatureFlagService(true, null, null);
        FeatureFlagService disabled = new SimpleFeatureFlagService(false, null, null);

        assertTrue(enabled.isEnabled("service", "other"));
        assertTrue(disabled.isDisabled("service", "other"));
    }

    @Test
    void enabledTargetsMatchServiceAndApplication() {
        for (boolean defaultFlag : List.of(true, false)) {
            FeatureFlagService service = new SimpleFeatureFlagService(defaultFlag,
                    List.of(new FeatureFlagTarget("service-a", "app"), new FeatureFlagTarget("service-b", "other")), null);

            assertTrue(service.isEnabled("service-a", "app"));
            assertTrue(service.isEnabled("service-b", "other"));
            assertEquals(defaultFlag, service.isEnabled("service-a", "unlisted"));
            assertEquals(defaultFlag, service.isEnabled("service-c", "app"));
            assertEquals(defaultFlag, service.isEnabled("service-a", "other"));
            assertEquals(defaultFlag, service.isEnabled("service-b", "app"));
        }
    }

    @Test
    void disabledTargetsMatchServiceAndApplication() {
        for (boolean defaultFlag : List.of(true, false)) {
            FeatureFlagService service = new SimpleFeatureFlagService(defaultFlag, null,
                    List.of(new FeatureFlagTarget("service-a", "app"), new FeatureFlagTarget("service-b", "other")));

            assertTrue(service.isDisabled("service-a", "app"));
            assertTrue(service.isDisabled("service-b", "other"));
            assertEquals(defaultFlag, service.isEnabled("service-a", "unlisted"));
            assertEquals(defaultFlag, service.isEnabled("service-c", "app"));
            assertEquals(defaultFlag, service.isEnabled("service-a", "other"));
            assertEquals(defaultFlag, service.isEnabled("service-b", "app"));
        }
    }

    @Test
    void disabledIfClashed() {
        for (boolean defaultFlag : List.of(true, false)) {
            FeatureFlagService service = new SimpleFeatureFlagService(defaultFlag,
                    List.of(new FeatureFlagTarget("service", "app")),
                    List.of(new FeatureFlagTarget("service", "app")));

            assertTrue(service.isDisabled("service", "app"));
        }
    }

    @Test
    void bothListsPreserveDefaultForUnmatchedTargets() {
        for (boolean defaultFlag : List.of(true, false)) {
            FeatureFlagService service = new SimpleFeatureFlagService(defaultFlag,
                    List.of(new FeatureFlagTarget("service", "enabled")),
                    List.of(new FeatureFlagTarget("service", "disabled")));

            assertTrue(service.isEnabled("service", "enabled"));
            assertTrue(service.isDisabled("service", "disabled"));
            assertEquals(defaultFlag, service.isEnabled("service", "other"));
            assertEquals(defaultFlag, service.isEnabled("other-service", "enabled"));
            assertEquals(defaultFlag, service.isEnabled("other-service", "disabled"));
        }
    }

    @Test
    void enabledTargetsAreCopiedAtConstruction() {
        List<FeatureFlagTarget> targets = new ArrayList<>(List.of(new FeatureFlagTarget("service", "app")));
        FeatureFlagService service = new SimpleFeatureFlagService(false, targets, null);

        targets.clear();
        targets.add(new FeatureFlagTarget("service", "other"));

        assertTrue(service.isEnabled("service", "app"));
        assertFalse(service.isEnabled("service", "other"));
    }

    @Test
    void disabledTargetsAreCopiedAtConstruction() {
        List<FeatureFlagTarget> targets = new ArrayList<>(List.of(new FeatureFlagTarget("service", "app")));
        FeatureFlagService service = new SimpleFeatureFlagService(true, null, targets);

        targets.clear();
        targets.add(new FeatureFlagTarget("service", "other"));

        assertTrue(service.isDisabled("service", "app"));
        assertTrue(service.isEnabled("service", "other"));
    }

    @Test
    void applicationNamesAreLiteral() {
        FeatureFlagService service = new SimpleFeatureFlagService(false,
                List.of(new FeatureFlagTarget("service", "app^name"), new FeatureFlagTarget("service", "app\\name")), null);

        assertTrue(service.isEnabled("service", "app^name"));
        assertTrue(service.isEnabled("service", "app\\name"));
    }
}
