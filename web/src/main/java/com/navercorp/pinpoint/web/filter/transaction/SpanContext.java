/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.web.filter.transaction;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.filter.agent.AgentFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanContext {
    private final List<SpanBo> nodeList;
    private final ServiceTypeRegistryService serviceTypeRegistryService;

    public SpanContext(List<SpanBo> nodeList,
                       ServiceTypeRegistryService serviceTypeRegistryService) {
        this.nodeList = Objects.requireNonNull(nodeList, "nodeList");
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
    }

    public List<SpanBo> findNode(String findApplicationName, List<ServiceType> findServiceCode, AgentFilter agentFilter) {
        List<SpanBo> findList = null;
        for (SpanBo span : nodeList) {
            final ServiceType applicationServiceType = serviceTypeRegistryService.findServiceType(span.getApplicationServiceType());
            if (findApplicationName.equals(span.getApplicationId()) && includeServiceType(findServiceCode, applicationServiceType)) {
                // apply preAgentFilter
                if (agentFilter.accept(span.getAgentId())) {
                    if (findList == null) {
                        findList = new ArrayList<>();
                    }
                    findList.add(span);
                }
            }
        }
        if (findList == null) {
            return Collections.emptyList();
        }
        return findList;
    }

    public boolean includeServiceType(List<ServiceType> serviceTypeList, ServiceType targetServiceType) {
        for (ServiceType serviceType : serviceTypeList) {
            if (serviceType == targetServiceType) {
                return Filter.ACCEPT;
            }
        }
        return Filter.REJECT;
    }

    public ServiceType findServiceType(short serviceType) {
        return serviceTypeRegistryService.findServiceType(serviceType);
    }
}
