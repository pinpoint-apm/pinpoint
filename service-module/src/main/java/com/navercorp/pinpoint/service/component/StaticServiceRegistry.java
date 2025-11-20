/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.service.component;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticServiceRegistry {

    private final Map<String, ServiceUid> serviceNameLookupMap;
    private final IntObjectMap<String> serviceUidLookupMap;

    public StaticServiceRegistry() {
        this.serviceNameLookupMap = createNameLookupTable();
        this.serviceUidLookupMap = createUidLookupTable(serviceNameLookupMap);
    }

    private Map<String, ServiceUid> createNameLookupTable() {
        Map<String, ServiceUid> map = new HashMap<>();
        try {
            for (Field field : ServiceUid.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    if (field.getType() == ServiceUid.class) {
                        ServiceUid serviceUid = (ServiceUid) field.get(ServiceUid.class);
                        map.put(field.getName(), serviceUid);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("static serviceUid map initialization fail", e);
        }
        return map;
    }

    private IntObjectMap<String> createUidLookupTable(Map<String, ServiceUid> serviceNameToUidMap) {
        MutableIntObjectMap<String > map = IntObjectMaps.mutable.ofInitialCapacity(serviceNameToUidMap.size());
        for (Map.Entry<String, ServiceUid> entry : serviceNameToUidMap.entrySet()) {
            map.put(entry.getValue().getUid(), entry.getKey());
        }
        return map;
    }

    // ignore case for serviceName
    public ServiceUid getServiceUid(String serviceName) {
        return serviceNameLookupMap.get(serviceName.toUpperCase());
    }

    public String getServiceName(ServiceUid serviceUid) {
        return serviceUidLookupMap.get(serviceUid.getUid());
    }

    public String getServiceName(int uid) {
        return serviceUidLookupMap.get(uid);
    }

    public List<String> getServiceNames() {
        return new ArrayList<>(serviceNameLookupMap.keySet());
    }

    public boolean contains(String serviceName) {
        return serviceNameLookupMap.containsKey(serviceName.toUpperCase());
    }

    public boolean contains(ServiceUid serviceUid) {
        return serviceUidLookupMap.containsKey(serviceUid.getUid());
    }
}
