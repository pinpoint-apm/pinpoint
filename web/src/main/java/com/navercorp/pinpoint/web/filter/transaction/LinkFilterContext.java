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
import com.navercorp.pinpoint.web.filter.URLPatternFilter;
import com.navercorp.pinpoint.web.filter.agent.AgentFilter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LinkFilterContext {

    private final SpanContainer spanContainer;

    private final String fromApplicationName;
    private final List<ServiceType> fromServiceDescList;
    private final AgentFilter fromAgentFilter;


    private final String toApplicationName;
    private final List<ServiceType> toServiceDescList;
    private final AgentFilter toAgentFilter;

    public LinkFilterContext(SpanContainer spanContainer,

                             String fromApplicationName,
                             List<ServiceType> fromServiceDescList,
                             AgentFilter fromAgentFilter,

                             String toApplicationName,
                             List<ServiceType> toServiceDescList,
                             AgentFilter toAgentFilter) {
        this.spanContainer = Objects.requireNonNull(spanContainer, "spanContainer");


        this.fromApplicationName = Objects.requireNonNull(fromApplicationName, "fromApplicationName");
        this.fromServiceDescList = Objects.requireNonNull(fromServiceDescList, "fromServiceDescList");
        this.fromAgentFilter = Objects.requireNonNull(fromAgentFilter, "fromAgentFilter");

        this.toApplicationName = Objects.requireNonNull(toApplicationName, "toApplicationName");
        this.toServiceDescList = Objects.requireNonNull(toServiceDescList, "toServiceDescList");
        this.toAgentFilter = Objects.requireNonNull(toAgentFilter, "toAgentFilter");

    }


    public List<SpanBo> findFromNode() {
        return spanContainer.findNode(fromApplicationName, fromServiceDescList, fromAgentFilter);
    }

//    public List<SpanBo> findToNode() {
//        return spanContainer.findNode(toApplicationName, toServiceDescList, toAgentFilter);
//    }

    public List<SpanBo> findToNode(URLPatternFilter acceptURLFilter) {
        final List<SpanBo> node = spanContainer.findNode(toApplicationName, toServiceDescList, toAgentFilter);
        if (acceptURLFilter == null) {
            return node;
        }
        if (!acceptURLFilter.accept(node)) {
            return Collections.emptyList();
        }
        return node;
    }

    public ServiceType findServiceType(short serviceType) {
        return spanContainer.findServiceType(serviceType);
    }

    public boolean isToNode(String applicationId, ServiceType serviceType) {
        return this.toApplicationName.equals(applicationId) && spanContainer.includeServiceType(this.toServiceDescList, serviceType);
    }

    public boolean isToApplicationName(String applicationId) {
        return toApplicationName.equals(applicationId);
    }

    public boolean isFromApplicationName(String applicationId) {
        return fromApplicationName.equals(applicationId);
    }
}
