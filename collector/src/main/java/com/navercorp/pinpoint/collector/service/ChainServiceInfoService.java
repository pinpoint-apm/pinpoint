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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
@Service
@Primary
public class ChainServiceInfoService implements ServiceInfoService {

    private final ServiceInfoService delegate;
    private final ServiceInfoService staticServiceInfoService;

    public ChainServiceInfoService(
            @Qualifier("serviceInfoServiceImpl") ServiceInfoService delegate,
            @Qualifier("staticServiceInfoService") ServiceInfoService staticServiceInfoService
    ) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.staticServiceInfoService = Objects.requireNonNull(staticServiceInfoService, "staticServiceInfoService");
    }

    @Override
    public ServiceId getServiceId(String serviceName) {
        ServiceId serviceId = staticServiceInfoService.getServiceId(serviceName);
        if (serviceId != null) {
            return serviceId;
        }
        return delegate.getServiceId(serviceName);
    }

    @Override
    public ServiceInfo getServiceInfo(ServiceId serviceId) {
        ServiceInfo service = staticServiceInfoService.getServiceInfo(serviceId);
        if (service != null) {
            return service;
        }
        return delegate.getServiceInfo(serviceId);
    }

    @Override
    public void insertAgentInfo(AgentInfoBo agent) {
        delegate.insertAgentInfo(agent);
    }

}
