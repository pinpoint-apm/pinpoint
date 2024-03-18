/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.grpc;

import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import io.grpc.Metadata;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class AgentHeaderFactory implements HeaderFactory {

    private final AgentId agentId;
    private final String agentName;
    private final String applicationName;
    private final String serviceName;
    private final long agentStartTime;
    private final int serviceType;

    public AgentHeaderFactory(AgentId agentId, String agentName, String applicationName, String serviceName,
                              int serviceType, long agentStartTime) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentName = agentName;
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.serviceType = serviceType;
        this.agentStartTime = agentStartTime;
    }

    public Metadata newHeader() {
        Metadata headers = new Metadata();
        headers.put(Header.AGENT_ID_KEY, agentId.value());
        headers.put(Header.APPLICATION_NAME_KEY, applicationName);
        headers.put(Header.SERVICE_NAME_KEY, serviceName);
        headers.put(Header.SERVICE_TYPE_KEY, Integer.toString(serviceType));
        headers.put(Header.AGENT_START_TIME_KEY, Long.toString(agentStartTime));
        if (!StringUtils.isEmpty(agentName)) {
            headers.put(Header.AGENT_NAME_KEY, agentName);
        }
        return headers;
    }
}
