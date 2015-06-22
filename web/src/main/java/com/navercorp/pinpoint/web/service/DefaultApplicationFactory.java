/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author emeroad
 */
@Component
public class DefaultApplicationFactory implements ApplicationFactory {

    @Autowired
    private ServiceTypeRegistryService registry;

    @Override
    public Application createApplication(String applicationName, short serviceTypeCode) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }

        final ServiceType serviceType = registry.findServiceType(serviceTypeCode);
        return new Application(applicationName, serviceType);
    }

    @Override
    public Application createApplication(String applicationName, ServiceType serviceType) {
        return new Application(applicationName, serviceType);
    }

    @Override
    public Application createApplicationByTypeName(String applicationName, String serviceTypeName) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (serviceTypeName == null) {
            throw new NullPointerException("serviceTypeName must not be null");
        }

        final ServiceType serviceType = registry.findServiceTypeByName(serviceTypeName);
        return new Application(applicationName, serviceType);
    }

}
