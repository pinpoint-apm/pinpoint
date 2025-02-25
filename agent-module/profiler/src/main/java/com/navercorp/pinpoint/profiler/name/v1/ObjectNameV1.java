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

package com.navercorp.pinpoint.profiler.name.v1;

import com.navercorp.pinpoint.profiler.name.ObjectName;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ObjectNameV1 implements ObjectName {
    private final String agentId;
    private final String agentName;
    private final String applicationName;

    public ObjectNameV1(String agentId, String agentName, String applicationName) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentName = agentName;
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
    }

    @Override
    public int getVersion() {
        return VERSION_V1;
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public String getAgentName() {
        return agentName;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public String getServiceName() {
        return null;
    }

    @Override
    public String toString() {
        return "ObjectNameV1{" +
                "agentId='" + agentId + '\'' +
                ", agentName='" + agentName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                '}';
    }
}
