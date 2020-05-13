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
import com.navercorp.pinpoint.web.filter.agent.AgentFilter;

import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class NodeContext {

    private final SpanContext spanContext;

    private final String applicationName;
    private final List<ServiceType> serviceDescList;
    private final AgentFilter agentFilter;

    public NodeContext(SpanContext spanContext, String applicationName, List<ServiceType> serviceDescList, AgentFilter agentFilter){
        this.spanContext = Objects.requireNonNull(spanContext, "spanContext");
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.serviceDescList = Objects.requireNonNull(serviceDescList, "serviceDescList");
        this.agentFilter = Objects.requireNonNull(agentFilter, "agentFilter");
    }

    public List<SpanBo> findApplicationNode() {
        return spanContext.findNode(applicationName, serviceDescList, agentFilter);
    }


    public String getApplicationName() {
        return applicationName;
    }

    public List<ServiceType> getServiceDescList() {
        return serviceDescList;
    }

    public AgentFilter getAgentFilter() {
        return agentFilter;
    }

    @Override
    public String toString() {
        return "NodeContext{" +
                "spanContext=" + spanContext +
                ", applicationName='" + applicationName + '\'' +
                ", serviceDescList=" + serviceDescList +
                ", agentFilter=" + agentFilter +
                '}';
    }
}
