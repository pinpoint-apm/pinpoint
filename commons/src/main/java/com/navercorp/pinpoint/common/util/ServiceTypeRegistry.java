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

package com.navercorp.pinpoint.common.util;

import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.util.apache.IntHashMap;
import com.navercorp.pinpoint.common.util.apache.IntHashMapUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author emeroad
 */
public class ServiceTypeRegistry {

    private final IntHashMap<ServiceType> codeLookupTable;

    private ServiceTypeRegistry() {
        this.codeLookupTable = new IntHashMap<ServiceType>();
    }

    private ServiceTypeRegistry(IntHashMap<ServiceType> buildMap) {
        if (buildMap == null) {
            throw new NullPointerException("codeLookupTable must not be null");
        }
        this.codeLookupTable = buildMap;
    }

    public ServiceType findServiceType(short code) {
        ServiceType serviceType = this.codeLookupTable.get(code);
        if (serviceType == null) {
            return ServiceType.UNDEFINED;
        }
        return serviceType;
    }


    public static class Builder {

        private final HashMap<Integer, ServiceType> buildMap = new HashMap<Integer, ServiceType>();

        public void addServiceType(ServiceType serviceType) {
            if (serviceType == null) {
                throw new NullPointerException("serviceType must not be null");
            }
            int code = serviceType.getCode();
            final ServiceType exist = this.buildMap.put(code, serviceType);
            if (exist != null) {
                throw new IllegalStateException("already exist. serviceType:" + serviceType + ", exist:" + exist);
            }
        }


        public ServiceTypeRegistry build() {
            IntHashMap<ServiceType> copy = IntHashMapUtils.copy(buildMap);
            return new ServiceTypeRegistry(copy);
        }
    }

}
