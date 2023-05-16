/*
 * Copyright 2018 NAVER Corp.
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
 */

package com.navercorp.pinpoint.profiler.plugin;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginGlobalContext;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class DefaultProfilerPluginGlobalContext implements ProfilerPluginGlobalContext {

    private final ProfilerConfig profilerConfig;
    private final ServiceType configuredApplicationType;
    private ServiceType applicationType = null;

    public DefaultProfilerPluginGlobalContext(ProfilerConfig profilerConfig, ServiceType configuredApplicationType) {
        this.profilerConfig = Objects.requireNonNull(profilerConfig, "profilerConfig");
        this.configuredApplicationType = Objects.requireNonNull(configuredApplicationType, "configuredApplicationType");
    }

    @Override
    public ProfilerConfig getConfig() {
        return profilerConfig;
    }

    @Override
    public ServiceType getConfiguredApplicationType() {
        return configuredApplicationType;
    }

    @Override
    public ServiceType getApplicationType() {
        return applicationType;
    }

    @Override
    public boolean registerApplicationType(ServiceType applicationType) {
        Objects.requireNonNull(applicationType, "applicationType");

        if (this.applicationType == null) {
            this.applicationType = applicationType;
            return true;
        }
        return false;
    }
}
