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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author HyunGil Jeong
 */
public class ConfiguredApplicationTypeProvider implements Provider<ServiceType> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String applicationTypeString;
    private final ServiceTypeRegistryService serviceTypeRegistryService;

    @Inject
    public ConfiguredApplicationTypeProvider(ProfilerConfig profilerConfig, ServiceTypeRegistryService serviceTypeRegistryService) {
        Assert.requireNonNull(profilerConfig, "profilerConfig");
        this.serviceTypeRegistryService = Assert.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
        this.applicationTypeString = profilerConfig.getApplicationServerType();
    }

    @Override
    public ServiceType get() {
        ServiceType applicationType = serviceTypeRegistryService.findServiceTypeByName(applicationTypeString);
        if (applicationType == null) {
            return ServiceType.UNDEFINED;
        }
        if (ServiceType.UNDEFINED.equals(applicationType)) {
            return ServiceType.UNDEFINED;
        }
        if (applicationType.isWas()) {
            return applicationType;
        }
        logger.warn("Invalid application type configured : {}, defaulting to {}", applicationType, ServiceType.UNDEFINED);
        return ServiceType.UNDEFINED;
    }
}
