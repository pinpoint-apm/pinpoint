/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeBuilder;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ServiceTypeRegistryMockFactory {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Map<Integer, ServiceType> serviceTypeMap = new HashMap<>();

    public void addServiceTypeMock(ServiceType serviceType) {
        Objects.requireNonNull(serviceType, "serviceType");
        this.serviceTypeMap.put((int) serviceType.getCode(), serviceType);
    }

    public void addServiceTypeMock(short typeCode, String typeName, ServiceTypeProperty... serviceTypeProperties) {
        // setup serviceType
        ServiceTypeBuilder builder = new ServiceTypeBuilder(typeCode, typeName, typeName);
        for (ServiceTypeProperty serviceTypeProperty : serviceTypeProperties) {
            switch (serviceTypeProperty) {
                case TERMINAL:
                    builder.terminal(true);
                    break;
                case QUEUE:
                    builder.queue(true);
                    break;
                case RECORD_STATISTICS:
                    builder.recordStatistics(true);
                    break;
                case INCLUDE_DESTINATION_ID:
                    builder.includeDestinationId(true);
                    break;
                case ALIAS:
                    builder.alias(true);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown serviceTypeProperty : " + serviceTypeProperty);
            }
        }

        ServiceType serviceType = builder.build();

        this.serviceTypeMap.put((int) serviceType.getCode(), serviceType);
    }

    public ServiceType getServiceTypeMock(int typeCode) {
        return this.serviceTypeMap.get(typeCode);
    }


    public ServiceTypeRegistryService createMockServiceTypeRegistryService() {

        return new ServiceTypeRegistryService() {
            @Override
            public ServiceType findServiceType(int serviceType) {
                return serviceTypeMap.get(serviceType);
            }

            @Override
            public ServiceType findServiceTypeByName(String typeName) {
                for (ServiceType serviceType : serviceTypeMap.values()) {
                    if (serviceType.getName().equals(typeName)) {
                        return serviceType;
                    }
                }
                throw new IllegalArgumentException("not found");
            }

            @Override
            public List<ServiceType> findDesc(String desc) {
                return serviceTypeMap.values().stream()
                        .filter(serviceType -> serviceType.getDesc().equals(desc))
                        .collect(Collectors.toList());
            }
        };
    }
}
