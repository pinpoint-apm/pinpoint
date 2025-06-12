package com.navercorp.pinpoint.uid.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.navercorp.pinpoint.common.server.uid.ServiceUid.DEFAULT_SERVICE_UID;

public class ServiceGroupV3Service implements ServiceGroupService {

    private final String defaultServiceName = "default";

    @Override
    public List<String> selectAllServiceNames() {
        return Collections.singletonList("");
    }

    @Override
    public String selectServiceName(ServiceUid serviceUid) {
        if (serviceUid == null || serviceUid.equals(DEFAULT_SERVICE_UID)) {
            return defaultServiceName;
        }
        return null;
    }

    @Override
    public ServiceUid getServiceUid(String serviceName) {
        return DEFAULT_SERVICE_UID;
    }

    @Override
    public void createService(String serviceName) {
    }

    @Override
    public void deleteService(String serviceName) {

    }
}
