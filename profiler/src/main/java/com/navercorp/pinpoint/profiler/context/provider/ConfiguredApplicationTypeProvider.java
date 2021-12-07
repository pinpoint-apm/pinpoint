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
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import java.util.Objects;

import com.navercorp.pinpoint.profiler.instrument.config.InstrumentConfig;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author HyunGil Jeong
 */
public class ConfiguredApplicationTypeProvider implements Provider<ServiceType> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final String applicationTypeString;
    private final ServiceTypeRegistryService serviceTypeRegistryService;

    @Inject
    public ConfiguredApplicationTypeProvider(InstrumentConfig instrumentConfig, ServiceTypeRegistryService serviceTypeRegistryService) {
        Objects.requireNonNull(instrumentConfig, "instrumentConfig");
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
        this.applicationTypeString = instrumentConfig.getApplicationServerType();
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
