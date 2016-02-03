/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.scatter;

import org.apache.commons.lang3.StringUtils;

/**
 * @Author Taejin Koo
 */
public class ScatterAgentInfo {

    private static final long UNKNOWN_AGENT_START_TIME = -1;

    private final String agentId;
    private final long agentStartTime;

    public ScatterAgentInfo(String agentId) {
        this(agentId, UNKNOWN_AGENT_START_TIME);
    }

    public ScatterAgentInfo(String agentId, long agentStartTime) {
        if (agentId == null) {
            throw new NullPointerException("agentId may not be null.");
        }

        this.agentId = agentId;
        this.agentStartTime = agentStartTime;
    }

    public String getAgentId() {
        return agentId;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    @Override
    public int hashCode() {
        int result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (int) (agentStartTime ^ (agentStartTime >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ScatterAgentInfo that = (ScatterAgentInfo) o;

        return agentStartTime == that.getAgentStartTime() && StringUtils.equals(agentId, that.getAgentId());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append("ScatterAgentInfo{");
        sb.append("agentId='");
        sb.append(agentId).append('\'');
        if (agentStartTime != UNKNOWN_AGENT_START_TIME) {
            sb.append(", agentStartTime=");
            sb.append(agentStartTime);
        }
        sb.append("}");

        return sb.toString();
    }

}
