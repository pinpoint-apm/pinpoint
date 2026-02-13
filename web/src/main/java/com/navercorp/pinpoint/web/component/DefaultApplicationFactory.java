/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.web.component;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Service;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author emeroad
 */
@Component
public class DefaultApplicationFactory implements ApplicationFactory {

    private final ServiceTypeRegistryService registry;

    public DefaultApplicationFactory(ServiceTypeRegistryService registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    static Application newApplication(Service service, String applicationName, ServiceType serviceType) {
        return new Application(service, applicationName, serviceType);
    }

    static Service requireDefaultService(int serviceUid) {
        if (Service.DEFAULT.getUid() != serviceUid) {
            throw new UnsupportedOperationException("Unsupported serviceUid: " + serviceUid);
        }
        return Service.DEFAULT;
    }

    @Override
    public Application createApplication(Service service, String applicationName, ServiceType serviceType) {
        Objects.requireNonNull(service, "service");
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(serviceType, "serviceType");

        return newApplication(service, applicationName, serviceType);
    }

    @Override
    public Application createApplication(Service service, String applicationName, int serviceTypeCode) {
        Objects.requireNonNull(service, "service");
        Objects.requireNonNull(applicationName, "applicationName");

        final ServiceType serviceType = registry.findServiceType(serviceTypeCode);
        return newApplication(service, applicationName, serviceType);
    }

    @Override
    public Application createApplication(int serviceUid, String applicationName, int serviceTypeCode) {
        Service service = requireDefaultService(serviceUid);

        final ServiceType serviceType = registry.findServiceType(serviceTypeCode);
        return newApplication(service, applicationName, serviceType);
    }

    @Override
    public Application createApplication(String applicationName, int serviceTypeCode) {
        Objects.requireNonNull(applicationName, "applicationName");

        final ServiceType serviceType = registry.findServiceType(serviceTypeCode);
        return newApplication(Service.DEFAULT, applicationName, serviceType);
    }

    @Override
    public Application createApplication(String applicationName, ServiceType serviceType) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(serviceType, "serviceType");

        return newApplication(Service.DEFAULT, applicationName, serviceType);
    }

    @Override
    public Application createApplicationByTypeName(String applicationName, String serviceTypeName) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(serviceTypeName, "serviceTypeName");

        final ServiceType serviceType = registry.findServiceTypeByName(serviceTypeName);
        return newApplication(Service.DEFAULT, applicationName, serviceType);
    }

    @Override
    public Application createApplicationByTypeName(int serviceUid, String applicationName, String serviceTypeName) {
        Service service = requireDefaultService(serviceUid);
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(serviceTypeName, "serviceTypeName");

        final ServiceType serviceType = registry.findServiceTypeByName(serviceTypeName);
        return newApplication(service, applicationName, serviceType);
    }

    @Override
    public Application createApplicationByTypeName(Service service, String applicationName, String serviceTypeName) {
        Objects.requireNonNull(service, "service");
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(serviceTypeName, "serviceTypeName");

        final ServiceType serviceType = registry.findServiceTypeByName(serviceTypeName);
        return newApplication(service, applicationName, serviceType);
    }

}
