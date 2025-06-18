package com.navercorp.pinpoint.service.component;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticServiceRegistry {

    private final Map<String, ServiceUid> serviceNameLookupMap;
    private final Map<Integer, String> serviceUidLookupMap;

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

    private Map<Integer, String> createUidLookupTable(Map<String, ServiceUid> serviceNameToUidMap) {
        Map<Integer, String> map = new HashMap<>(serviceNameToUidMap.size());
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
}
