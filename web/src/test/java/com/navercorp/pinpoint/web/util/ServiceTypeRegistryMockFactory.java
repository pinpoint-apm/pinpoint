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

import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeCategory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ServiceTypeRegistryMockFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<Short, ServiceType> serviceTypeMap = new HashMap<>();

    public void addServiceTypeMock(short typeCode, String typeName, ServiceTypeProperty... serviceTypeProperties) {
        // setup serviceType
        ServiceType mockServiceType = mock(ServiceType.class);
        when(mockServiceType.getCode()).thenReturn(typeCode);
        when(mockServiceType.getName()).thenReturn(typeName);
        when(mockServiceType.getDesc()).thenReturn(typeName);
        when(mockServiceType.getHistogramSchema()).thenReturn(ServiceTypeCategory.findCategory(typeCode).getHistogramSchema());

        if (ServiceType.USER.getName().equals(typeName)) {
            when(mockServiceType.isUser()).thenReturn(true);
        }
        if (ServiceType.UNKNOWN.getName().equals(typeName)) {
            when(mockServiceType.isUnknown()).thenReturn(true);
        }
        if (ServiceTypeCategory.SERVER.contains(typeCode)) {
            when(mockServiceType.isWas()).thenReturn(true);
        }
        if (ServiceTypeCategory.RPC.contains(typeCode)) {
            when(mockServiceType.isRpcClient()).thenReturn(true);
        }

        for (ServiceTypeProperty serviceTypeProperty : serviceTypeProperties) {
            switch (serviceTypeProperty) {
                case TERMINAL:
                    when(mockServiceType.isTerminal()).thenReturn(true);
                    break;
                case QUEUE:
                    when(mockServiceType.isQueue()).thenReturn(true);
                    break;
                case RECORD_STATISTICS:
                    when(mockServiceType.isRecordStatistics()).thenReturn(true);
                    break;
                case INCLUDE_DESTINATION_ID:
                    when(mockServiceType.isIncludeDestinationId()).thenReturn(true);
                    break;
                case ALIAS:
                    when(mockServiceType.isAlias()).thenReturn(true);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown serviceTypeProperty : " + serviceTypeProperty);
            }
        }

        this.serviceTypeMap.put(typeCode, mockServiceType);
    }

    public ServiceType getServiceTypeMock(short typeCode) {
        return this.serviceTypeMap.get(typeCode);
    }


    public ServiceTypeRegistryService createMockServiceTypeRegistryService() {

        final ServiceTypeRegistryService serviceTypeRegistryService = mock(ServiceTypeRegistryService.class);
        for (ServiceType serviceType : serviceTypeMap.values()) {
            // setup serviceRegistry
            final String serviceTypeName = serviceType.getName();
            final short serviceTypeCode = serviceType.getCode();
            when(serviceTypeRegistryService.findServiceTypeByName(serviceTypeName)).thenReturn(serviceType);
            when(serviceTypeRegistryService.findServiceType(serviceTypeCode)).thenReturn(serviceType);
            when(serviceTypeRegistryService.findDesc(serviceTypeName)).thenReturn(Collections.singletonList(serviceType));
        }

        return serviceTypeRegistryService;
    }
}
