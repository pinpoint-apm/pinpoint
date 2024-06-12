/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.common.id.ServiceId;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.ServiceInfo;
import org.springframework.stereotype.Service;

/**
 * @author youngjin.kim2
 */
@Service
public class StaticServiceInfoService implements ServiceInfoService {

    private static final ServiceInfo[] reservations = new ServiceInfo[] {
        new ServiceInfo(ServiceId.DEFAULT_ID, ServiceId.DEFAULT_SERVICE_NAME),
    };

    @Override
    public ServiceId getServiceId(String serviceName) {
        for (ServiceInfo reservation : reservations) {
            if (reservation.name().equals(serviceName)) {
                return reservation.id();
            }
        }
        return null;
    }

    @Override
    public ServiceInfo getServiceInfo(ServiceId serviceId) {
        for (ServiceInfo reservation : reservations) {
            if (reservation.id().equals(serviceId)) {
                return reservation;
            }
        }
        return null;
    }

    @Override
    public void insertAgentInfo(AgentInfoBo agent) {
        throw new UnsupportedOperationException("insertAgentInfo");
    }

}
