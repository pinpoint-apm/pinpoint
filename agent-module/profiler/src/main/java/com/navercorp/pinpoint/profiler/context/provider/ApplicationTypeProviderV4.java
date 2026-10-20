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
 */

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.profiler.instrument.config.ApplicationServerTypeMode;
import com.navercorp.pinpoint.profiler.instrument.config.InstrumentConfig;
import com.navercorp.pinpoint.profiler.name.ObjectName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Application type for {@link ApplicationServerTypeMode#V4}.
 * <ol>
 *     <li>{@code profiler.applicationservertype} is set to a valid WAS type: that type
 *     ({@link ConfiguredApplicationTypeProvider}).</li>
 *     <li>The agent belongs to the {@code DEFAULT} service (or has no service name, v1):
 *     {@link ServiceType#UNDEFINED}, the {@link ApplicationServerTypeMode#V1} rule and agent plugins detect the type.</li>
 *     <li>Any other service: {@link ServiceType#JAVA}, the single application type of a Java agent.</li>
 * </ol>
 */
public class ApplicationTypeProviderV4 implements Provider<ServiceType> {

    static final ServiceType V4_APPLICATION_TYPE = ServiceType.JAVA;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Provider<ServiceType> configuredTypeProvider;
    private final String serviceName;

    @Inject
    public ApplicationTypeProviderV4(InstrumentConfig instrumentConfig,
                                     ServiceTypeRegistryService serviceTypeRegistryService,
                                     ObjectName objectName) {
        this.configuredTypeProvider = new ConfiguredApplicationTypeProvider(instrumentConfig, serviceTypeRegistryService);
        Objects.requireNonNull(objectName, "objectName");
        this.serviceName = objectName.getServiceName();
    }

    @Override
    public ServiceType get() {
        final ServiceType configuredType = configuredTypeProvider.get();
        if (!ServiceType.UNDEFINED.equals(configuredType)) {
            return configuredType;
        }
        if (isDefaultService(serviceName)) {
            // the DEFAULT service keeps the V1 rule
            return ServiceType.UNDEFINED;
        }
        logger.info("serviceName={}. Defaulting application type to {}", serviceName, V4_APPLICATION_TYPE);
        return V4_APPLICATION_TYPE;
    }

    static boolean isDefaultService(String serviceName) {
        if (serviceName == null) {
            // v1 ObjectName has no service name
            return true;
        }
        return ObjectName.DEFAULT_SERVICE_NAME.equals(serviceName);
    }
}
