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

import com.navercorp.pinpoint.collector.dao.ServiceInfoDao;
import com.navercorp.pinpoint.common.id.ServiceId;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.ServiceInfo;
import com.navercorp.pinpoint.common.util.UuidUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
@Service
public class ServiceInfoServiceImpl implements ServiceInfoService {

    private final ServiceInfoDao serviceInfoDao;

    public ServiceInfoServiceImpl(ServiceInfoDao serviceInfoDao) {
        this.serviceInfoDao = Objects.requireNonNull(serviceInfoDao, "serviceInfoDao");
    }

    @Override
    public ServiceId getServiceId(String serviceName) {
        ServiceId serviceId = this.serviceInfoDao.getServiceId(serviceName);
        if (serviceId != null) {
            return serviceId;
        }

        ServiceId newServiceId = ServiceId.of(UuidUtils.createV4());
        return this.serviceInfoDao.putServiceIdIfAbsent(serviceName, newServiceId);
    }

    @Override
    public void insertAgentInfo(AgentInfoBo agent) {
        ServiceId serviceId = agent.getServiceId();
        String serviceName = agent.getServiceName();
        this.serviceInfoDao.ensurePut(serviceName, serviceId);
    }

    @Override
    public ServiceInfo getServiceInfo(ServiceId serviceId) {
        return this.serviceInfoDao.getServiceInfo(serviceId);
    }

}
