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

package com.navercorp.pinpoint.common.trace;

import com.navercorp.pinpoint.common.util.apache.IntHashMap;
import com.navercorp.pinpoint.common.util.apache.IntHashMapUtils;

import java.util.*;

/**
 * @author emeroad
 */
public class ServiceTypeRegistry {

    private final IntHashMap<ServiceType> codeLookupTable;

    private final Map<String, ServiceType> nameLookupTable;

    private final Map<String, List<ServiceType>> descLookupTable;

    private ServiceTypeRegistry() {
        this.codeLookupTable = new IntHashMap<ServiceType>();
        this.nameLookupTable = new HashMap<String, ServiceType>();
        this.descLookupTable = new HashMap<String, List<ServiceType>>();
    }

    private ServiceTypeRegistry(HashMap<Integer, ServiceType> buildMap) {
        if (buildMap == null) {
            throw new NullPointerException("codeLookupTable must not be null");
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

    public ServiceType findServiceType(short code) {
        final ServiceType serviceType = this.codeLookupTable.get(code);
        if (serviceType == null) {
            return ServiceType.UNDEFINED;
        }
        return serviceType;
    }

    public ServiceType findServiceTypeByName(String name) {
        final ServiceType serviceType = this.nameLookupTable.get(name);
        if (serviceType == null) {
            return ServiceType.UNDEFINED;
        }
        return serviceType;
    }

    @Deprecated
    public List<ServiceType> findDesc(String desc) {
        if (desc == null) {
            throw new NullPointerException("desc must not be null");
        }

        return descLookupTable.get(desc);
    }

    private Map<String, List<ServiceType>> buildDescLookupTable(Collection<ServiceType> serviceTypes) {
        final Map<String, List<ServiceType>> table = new HashMap<String, List<ServiceType>>();

        for (ServiceType serviceType : serviceTypes) {
            if (serviceType.isRecordStatistics()) {
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

            return new ServiceTypeRegistry(buildMap);
        }
    }

}
