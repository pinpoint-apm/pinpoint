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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Service;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class AgentIdEntry {

    private final Application application;
    private final String agentId;
    private final long agentStartTime;

    private final String agentName;
    private final long lastUpdated;

    public AgentIdEntry(Application application, String agentId, long agentStartTime,
                        long lastUpdated, @Nullable String agentName) {
        this.application = Objects.requireNonNull(application, "application");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentStartTime = agentStartTime;
        this.lastUpdated = lastUpdated;
        this.agentName = StringUtils.hasText(agentName) ? agentName : agentId;
    }

    @JsonIgnore
    public Application getApplication() {
        return application;
    }

    public Service getService() {
        return application.getService();
    }

    public String getApplicationName() {
        return application.getApplicationName();
    }

    public String getServiceType() {
        return application.getServiceType().getDesc();
    }

    public int getServiceTypeCode() {
        return application.getServiceTypeCode();
    }

    public String getAgentId() {
        return agentId;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    public String getAgentName() {
        return agentName;
    }

    @JsonIgnore
    public long getLastUpdated() {
        return lastUpdated;
    }
}
