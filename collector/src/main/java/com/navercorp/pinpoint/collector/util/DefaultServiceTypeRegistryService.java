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

package com.navercorp.pinpoint.collector.util;

import com.navercorp.pinpoint.collector.servlet.ServiceTypeLoader;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.TypeProviderLoader;
import com.navercorp.pinpoint.common.plugin.Type;
import com.navercorp.pinpoint.common.util.ServiceTypeRegistry;
import com.navercorp.pinpoint.common.util.StaticFieldLookUp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TODO FIX duplicated web DefaultServiceTypeRegistryService
 * @author emeroad
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class DefaultServiceTypeRegistryService implements ServiceTypeRegistryService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ServiceTypeRegistry registry;

    public DefaultServiceTypeRegistryService() {

        ServiceTypeRegistry.Builder builder = new ServiceTypeRegistry.Builder();

        StaticFieldLookUp<ServiceType> staticFieldLookUp = new StaticFieldLookUp<ServiceType>(ServiceType.class, ServiceType.class);
        List<ServiceType> lookup = staticFieldLookUp.lookup();
        for (ServiceType serviceType: lookup) {
            logger.debug("add Default ServiceType ServiceType:{}", serviceType);
            builder.addServiceType(serviceType);
        }

        List<Type> types = loadType();
        for (Type type : types) {
            logger.debug("add ServiceType ServiceType:{}, AnnotationKeyMatcher:{}", type.getServiceType(), type.getAnnotationKeyMatcher());
            builder.addServiceType(type.getServiceType());
        }
        this.registry = builder.build();
    }

    private List<Type> loadType() {
        // TODO remove static method
        final TypeProviderLoader typeProviderLoader = ServiceTypeLoader.getTypeProviderLoader();
        return typeProviderLoader.getTypes();
    }

    @Override
    public ServiceType findServiceType(short serviceType) {
        return registry.findServiceType(serviceType);
    }

}
