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

import org.springframework.lang.Nullable;

import java.util.List;

public class SimpleFeatureFlagService implements FeatureFlagService {
    private final boolean defaultFlag;

    private final List<FeatureFlagTarget> enabledApplications;
    private final List<FeatureFlagTarget> disabledApplications;

    public SimpleFeatureFlagService(boolean defaultFlag, @Nullable List<FeatureFlagTarget> enabledApplications, @Nullable List<FeatureFlagTarget> disabledApplications) {
        this.defaultFlag = defaultFlag;
        this.enabledApplications = enabledApplications == null ? List.of() : List.copyOf(enabledApplications);
        this.disabledApplications = disabledApplications == null ? List.of() : List.copyOf(disabledApplications);
    }

    @Override
    public boolean isEnabled(String serviceName, String applicationName) {
        FeatureFlagTarget target = new FeatureFlagTarget(serviceName, applicationName);
        // disabledApplications has higher priority
        if (disabledApplications.contains(target)) {
            return false;
        }
        if (enabledApplications.contains(target)) {
            return true;
        }
        return defaultFlag;
    }
}
