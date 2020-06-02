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

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import io.grpc.Metadata;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class AgentHeaderFactory implements HeaderFactory {

    private final String agentId;
    private final String applicationName;
    private final long agentStartTime;
    private final int serviceType;

    public AgentHeaderFactory(String agentId, String applicationName, long agentStartTime, int serviceType) {
        this.agentId = Assert.requireNonNull(agentId, "agentId");
        this.applicationName = Assert.requireNonNull(applicationName, "applicationName");
        this.agentStartTime = agentStartTime;
        this.serviceType = serviceType;
    }

    public Metadata newHeader() {
        Metadata headers = new Metadata();
        headers.put(Header.AGENT_ID_KEY, agentId);
        headers.put(Header.APPLICATION_NAME_KEY, applicationName);
        headers.put(Header.AGENT_START_TIME_KEY, Long.toString(agentStartTime));
        headers.put(Header.SERVICE_TYPE_KEY, Integer.toString(serviceType));
        return headers;
    }
}
