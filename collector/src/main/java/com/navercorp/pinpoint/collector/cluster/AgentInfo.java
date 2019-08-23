/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.cluster;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class AgentInfo {

    private final String applicationName;
    private final String agentId;
    private final long startTimestamp;
    private final String version;

    private final String agentKey;

    public AgentInfo(String applicationName, String agentId, long startTimestamp) {
        this(applicationName, agentId, startTimestamp, "");
    }

    public AgentInfo(String applicationName, String agentId, long startTimestamp, String version) {
        this.applicationName = applicationName;
        this.agentId = agentId;
        this.startTimestamp = startTimestamp;
        this.version = version;
        this.agentKey = createAgentKey(applicationName, agentId, startTimestamp);
    }

    private String createAgentKey(String applicationName, String agentId, long startTimestamp) {
        StringBuilder key = new StringBuilder();
        key.append(applicationName);
        key.append(":");
        key.append(agentId);
        key.append(":");
        key.append(startTimestamp);

        return key.toString();
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public String getVersion() {
        return version;
    }

    public String getAgentKey() {
        return agentKey;
    }

    public boolean equals(String applicationName, String agentId, long startTimestamp) {
        if (!this.applicationName.equals(applicationName)) {
            return false;
        }
        if (!this.agentId.equals(agentId)) {
            return false;
        }
        if (this.startTimestamp != startTimestamp) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentInfo agentInfo = (AgentInfo) o;
        return startTimestamp == agentInfo.startTimestamp &&
                Objects.equals(applicationName, agentInfo.applicationName) &&
                Objects.equals(agentId, agentInfo.agentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationName, agentId, startTimestamp);
    }

    @Override
    public String toString() {
        return "AgentInfo{" +
                getAgentKey() + ":"
                + version +
                '}';
    }

}
