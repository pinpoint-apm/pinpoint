/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.trace;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class ServiceTypeProvider {

    private static final ServiceTypeLocator UNREGISTERED = new ServiceTypeLocator() {
        @Override
        public ServiceType findServiceType(short code) {
            throw new IllegalStateException("ServiceTypeRegistry not registered");
        }

        @Override
        public ServiceType findServiceTypeByName(String name) {
            throw new IllegalStateException("ServiceTypeRegistry not registered");
        }

        @Override
        public List<ServiceType> findDesc(String name) {
            throw new IllegalStateException("ServiceTypeRegistry not registered");
        }
    };
    // must be non final  : TraceMetadataRegistrar
    private static ServiceTypeLocator registry = UNREGISTERED;

    private ServiceTypeProvider() {
        throw new AssertionError();
    }

    public static ServiceType getByCode(int serviceTypeCode) {
        ServiceType serviceType = registry.findServiceType((short) serviceTypeCode);
        if (ServiceType.UNDEFINED == serviceType) {
            throw new IllegalStateException("Unknown ServiceType code: " + serviceTypeCode);
        }
        return serviceType;
    }

    public static ServiceType getByName(String serviceTypeName) {
        ServiceType serviceType = registry.findServiceTypeByName(serviceTypeName);
        if (ServiceType.UNDEFINED == serviceType) {
            throw new IllegalStateException("Unknown ServiceType name: " + serviceTypeName);
        }
        return serviceType;
    }
}
