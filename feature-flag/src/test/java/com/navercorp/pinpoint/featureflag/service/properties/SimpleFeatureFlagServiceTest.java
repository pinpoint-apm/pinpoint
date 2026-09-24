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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleFeatureFlagServiceTest {
    private static final String DEFAULT_SERVICE = Service.DEFAULT.getServiceName();

    @Test
    void global() {
        FeatureFlagService enabled = new SimpleFeatureFlagService(true, null, null);
        FeatureFlagService disabled = new SimpleFeatureFlagService(false, null, null);

        assertTrue(enabled.isEnabled(DEFAULT_SERVICE, "other"));
        assertTrue(enabled.isEnabled("service-a", "other"));
        assertTrue(disabled.isDisabled(DEFAULT_SERVICE, "other"));
        assertTrue(disabled.isDisabled("service-a", "other"));
    }

    @Test
    void enabledForApp() {
        for (boolean defaultFlag : List.of(true, false)) {
            FeatureFlagService service = new SimpleFeatureFlagService(defaultFlag,
                    List.of(new FeatureFlagTarget("DEFAULT", "app")), null);

            assertTrue(service.isEnabled(DEFAULT_SERVICE, "app"));
            assertTrue(service.isEnabled("DEFAULT", "app"));
        }
    }

    @Test
    void disabledForApp() {
        for (boolean defaultFlag : List.of(true, false)) {
            FeatureFlagService service = new SimpleFeatureFlagService(defaultFlag,
                    null, List.of(new FeatureFlagTarget("DEFAULT", "app")));

            assertTrue(service.isDisabled(DEFAULT_SERVICE, "app"));
            assertTrue(service.isDisabled("DEFAULT", "app"));
        }
    }

    @Test
    void enabledForServiceAndApplication() {
        FeatureFlagService service = new SimpleFeatureFlagService(false,
                List.of(new FeatureFlagTarget("service-a", "app"), new FeatureFlagTarget("DEFAULT", "legacy")), null);

        assertTrue(service.isEnabled("service-a", "app"));
        assertFalse(service.isDisabled("service-a", "app"));
        assertFalse(service.isEnabled("service-b", "app"));
        assertFalse(service.isEnabled(DEFAULT_SERVICE, "app"));
        assertFalse(service.isEnabled("service-a", "other"));
        assertTrue(service.isEnabled(DEFAULT_SERVICE, "legacy"));
        assertFalse(service.isEnabled("service-a", "legacy"));
    }

    @Test
    void disabledForServiceAndApplication() {
        FeatureFlagService service = new SimpleFeatureFlagService(true, null,
                List.of(new FeatureFlagTarget("service-a", "app"), new FeatureFlagTarget("DEFAULT", "legacy")));

        assertTrue(service.isDisabled("service-a", "app"));
        assertTrue(service.isEnabled("service-b", "app"));
        assertTrue(service.isEnabled(DEFAULT_SERVICE, "app"));
        assertTrue(service.isEnabled("service-a", "other"));
        assertTrue(service.isDisabled(DEFAULT_SERVICE, "legacy"));
        assertTrue(service.isEnabled("service-a", "legacy"));
    }

    @Test
    void disabledIfClashed() {
        for (boolean defaultFlag : List.of(true, false)) {
            FeatureFlagService service = new SimpleFeatureFlagService(defaultFlag,
                    List.of(new FeatureFlagTarget("service-a", "app"), new FeatureFlagTarget("DEFAULT", "legacy")),
                    List.of(new FeatureFlagTarget("service-a", "app"), new FeatureFlagTarget("DEFAULT", "legacy")));

            assertTrue(service.isDisabled("service-a", "app"));
            assertTrue(service.isDisabled(DEFAULT_SERVICE, "legacy"));
        }
    }

    @Test
    void applicationNamesAreLiteral() {
        FeatureFlagService service = new SimpleFeatureFlagService(false,
                List.of(new FeatureFlagTarget("service-a", "app^name"), new FeatureFlagTarget("service-a", "app\\name")), null);

        assertTrue(service.isEnabled("service-a", "app^name"));
        assertTrue(service.isEnabled("service-a", "app\\name"));
        assertFalse(service.isEnabled("service-b", "app^name"));
    }
}
