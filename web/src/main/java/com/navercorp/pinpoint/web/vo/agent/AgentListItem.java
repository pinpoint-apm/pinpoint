/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo.agent;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.vo.Application;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class AgentListItem {

    private final int serviceUid;
    private final Application application;
    private final String agentId;
    private final long startTime;

    private final long lastUpdated;
    private final String agentName;

    private AgentStatus agentStatus;

    public AgentListItem(int serviceUid, Application application, String agentId, long startTime, long lastUpdated, @Nullable String agentName) {
        this.serviceUid = serviceUid;
        this.application = Objects.requireNonNull(application, "application");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.startTime = startTime;
        this.lastUpdated = lastUpdated;
        this.agentName = StringUtils.hasText(agentName) ? agentName : agentId;
    }

    public int getServiceUid() {
        return serviceUid;
    }

    public Application getApplication() {
        return application;
    }

    public String getAgentId() {
        return agentId;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public String getAgentName() {
        return agentName;
    }

    public AgentStatus getAgentStatus() {
        return agentStatus;
    }

    public void setAgentStatus(AgentStatus agentStatus) {
        this.agentStatus = agentStatus;
    }
}
