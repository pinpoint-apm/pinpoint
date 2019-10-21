/*
 * Copyright 2019 NAVER Corp.
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
 */

package com.navercorp.pinpoint.common.profiler.trace;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeLocator;
import com.navercorp.pinpoint.common.util.apache.IntHashMap;
import com.navercorp.pinpoint.common.util.apache.IntHashMapUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class ServiceTypeRegistry implements ServiceTypeLocator {

    private final IntHashMap<ServiceType> codeLookupTable;

    private final Map<String, ServiceType> nameLookupTable;

    private final Map<String, List<ServiceType>> descLookupTable;

    private ServiceTypeRegistry(HashMap<Integer, ServiceType> buildMap) {
        if (buildMap == null) {
            throw new NullPointerException("codeLookupTable");
        }
        this.codeLookupTable = IntHashMapUtils.copy(buildMap);
        this.nameLookupTable = buildNameLookupTable(buildMap.values());
        this.descLookupTable = buildDescLookupTable(buildMap.values());
    }

    private Map<String, ServiceType> buildNameLookupTable(Collection<ServiceType> serviceTypes) {
        final Map<String, ServiceType> copy = new HashMap<String, ServiceType>();

        for (ServiceType serviceType : serviceTypes) {
            final ServiceType duplicated = copy.put(serviceType.getName(), serviceType);
            if (duplicated  != null) {
                throw new IllegalStateException("duplicated ServiceType " + serviceType + " / " + duplicated);
            }
        }
        return copy;
    }

    @Override
    public ServiceType findServiceType(short code) {
        final ServiceType serviceType = this.codeLookupTable.get(code);
        if (serviceType == null) {
            return ServiceType.UNDEFINED;
        }
        return serviceType;
    }

    @Override
    public ServiceType findServiceTypeByName(String name) {
        final ServiceType serviceType = this.nameLookupTable.get(name);
        if (serviceType == null) {
            return ServiceType.UNDEFINED;
        }
        return serviceType;
    }

    @Override
    public List<ServiceType> findDesc(String desc) {
        if (desc == null) {
            throw new NullPointerException("desc");
        }
        return descLookupTable.get(desc);
    }

    private Map<String, List<ServiceType>> buildDescLookupTable(Collection<ServiceType> serviceTypes) {
        final Map<String, List<ServiceType>> table = new HashMap<String, List<ServiceType>>();

        for (ServiceType serviceType : serviceTypes) {
            if (serviceType.isRecordStatistics() || serviceType.isAlias()) {
                List<ServiceType> serviceTypeList = table.get(serviceType.getDesc());
                if (serviceTypeList == null) {
                    serviceTypeList = new ArrayList<ServiceType>();
                    table.put(serviceType.getDesc(), serviceTypeList);
                }
                serviceTypeList.add(serviceType);
            }
        }
        return unmodifiableMap(table);
    }

    private static Map<String, List<ServiceType>> unmodifiableMap(Map<String, List<ServiceType>> table) {
        // value of this table will be exposed. so make them unmodifiable.
        final Map<String, List<ServiceType>> copy = new HashMap<String, List<ServiceType>>(table.size());

        for (Map.Entry<String, List<ServiceType>> entry : table.entrySet()) {
            List<ServiceType> newValue = Collections.unmodifiableList(entry.getValue());
            copy.put(entry.getKey(), newValue);
        }
        return copy;
    }

    static class Builder {

        private final HashMap<Integer, ServiceType> buildMap = new HashMap<Integer, ServiceType>();

        void addServiceType(ServiceType serviceType) {
            if (serviceType == null) {
                throw new NullPointerException("serviceType");
            }
            int code = serviceType.getCode();
            final ServiceType exist = this.buildMap.put(code, serviceType);
            if (exist != null) {
                throw new IllegalStateException("already exist. serviceType:" + serviceType + ", exist:" + exist);
            }
        }

        ServiceTypeRegistry build() {
            return new ServiceTypeRegistry(buildMap);
        }
    }


}
