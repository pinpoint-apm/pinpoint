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

package com.navercorp.pinpoint.loader.service;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeLocator;

import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class DefaultServiceTypeRegistryService implements ServiceTypeRegistryService {

    private final ServiceTypeLocator serviceTypeLocator;

    public DefaultServiceTypeRegistryService(ServiceTypeLocator serviceTypeLocator) {
        this.serviceTypeLocator = Objects.requireNonNull(serviceTypeLocator, "serviceTypeLocator");
    }

    @Override
    public ServiceType findServiceType(int serviceType) {
        return serviceTypeLocator.findServiceType(serviceType);
    }

    public ServiceType findServiceTypeByName(String typeName) {
        return serviceTypeLocator.findServiceTypeByName(typeName);
    }

    @Override
    public List<ServiceType> findDesc(String desc) {
        return serviceTypeLocator.findDesc(desc);
    }

}
