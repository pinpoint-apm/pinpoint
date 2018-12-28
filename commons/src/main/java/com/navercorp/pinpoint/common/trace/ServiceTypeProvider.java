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

package com.navercorp.pinpoint.common.trace;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author HyunGil Jeong
 */
public class ServiceTypeProvider {

    private static final ServiceTypeProvider INSTANCE = new ServiceTypeProvider();

    private final ConcurrentMap<Short, ServiceType> codeMap = new ConcurrentHashMap<Short, ServiceType>();
    private final ConcurrentMap<String, ServiceType> nameMap = new ConcurrentHashMap<String, ServiceType>();

    private ServiceTypeProvider() {
    }

    public static ServiceType getByCode(int serviceTypeCode) {
        Short code = (short) serviceTypeCode;
        ServiceType serviceType = INSTANCE.codeMap.get(code);
        if (serviceType == null) {
            throw new IllegalStateException("Unknown ServiceType code: " + serviceTypeCode);
        }
        return serviceType;
    }

    public static ServiceType getByName(String serviceTypeName) {
        ServiceType serviceType = INSTANCE.nameMap.get(serviceTypeName);
        if (serviceType == null) {
            throw new IllegalStateException("Unknown ServiceType name: " + serviceTypeName);
        }
        return serviceType;
    }

    public static void register(ServiceType serviceType) {
        if (serviceType == null) {
            throw new IllegalArgumentException("serviceType must not be null");
        }
        INSTANCE.addServiceType(serviceType);
    }

    private void addServiceType(ServiceType serviceType) {
        short code = serviceType.getCode();
        String name = serviceType.getName();
        ServiceType prev = codeMap.putIfAbsent(code, serviceType);
        if (prev != null) {
            throw new IllegalStateException("Duplicate ServiceType code: " + code + " found for names: " + prev.getName() + ", " + name);
        }
        prev = nameMap.putIfAbsent(name, serviceType);
        if (prev != null) {
            throw new IllegalStateException("Duplicate ServiceType name: '" + name + "' found for codes: " + prev.getCode() + ", " + code);
        }
    }
}
