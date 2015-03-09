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

package com.navercorp.pinpoint.common.service;

import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.plugin.Type;
import com.navercorp.pinpoint.common.util.ServiceTypeRegistry;
import com.navercorp.pinpoint.common.util.StaticFieldLookUp;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author emeroad
 */
public class DefaultServiceTypeRegistryService implements ServiceTypeRegistryService {
    private final Logger logger = Logger.getLogger(DefaultServiceTypeRegistryService.class.getName());

    private final TypeLoaderService typeLoaderService;
    private final ServiceTypeRegistry registry;

    public DefaultServiceTypeRegistryService() {
        this(new DefaultTypeLoaderService());
    }


    public DefaultServiceTypeRegistryService(TypeLoaderService typeLoaderService) {
        if (typeLoaderService == null) {
            throw new NullPointerException("typeLoaderService must not be null");
        }
        this.typeLoaderService = typeLoaderService;
        this.registry = buildServiceTypeRegistry();
    }

    private ServiceTypeRegistry buildServiceTypeRegistry() {
        ServiceTypeRegistry.Builder builder = new ServiceTypeRegistry.Builder();

        StaticFieldLookUp<ServiceType> staticFieldLookUp = new StaticFieldLookUp<ServiceType>(ServiceType.class, ServiceType.class);
        List<ServiceType> lookup = staticFieldLookUp.lookup();
        for (ServiceType serviceType: lookup) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("add Default ServiceType:" + serviceType);
            }
            builder.addServiceType(serviceType);
        }

        final List<Type> types = loadType();
        for (Type type : types) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("add Plugin ServiceType:" + type.getServiceType());
            }
            builder.addServiceType(type.getServiceType());
        }


        return builder.build();
    }


    private List<Type> loadType() {
        return typeLoaderService.getTypes();
    }

    @Override
    public ServiceType findServiceType(short serviceType) {
        return registry.findServiceType(serviceType);
    }

    public ServiceType findServiceTypeByName(String typeName) {
        return registry.findServiceTypeByName(typeName);
    }

    @Override
    public List<ServiceType> findDesc(String desc) {
        return registry.findDesc(desc);
    }

}
