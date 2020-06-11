/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.server.util.DateTimeFormatUtils;

import java.time.Instant;

/**
 * @author Taejin Koo
 */
public class AgentCountStatistics {

    private int agentCount;
    private long timestamp;

    private AgentCountStatistics() {
    }

    public AgentCountStatistics(int agentCount, long timestamp) {
        this.agentCount = agentCount;
        this.timestamp = timestamp;
    }

    public int getAgentCount() {
        return agentCount;
    }

    public void setAgentCount(int agentCount) {
        this.agentCount = agentCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDateVal() {
        return DateTimeFormatUtils.formatSimple(timestamp);
    }

    public void setDateVal(Instant dateTime) {
        this.timestamp = dateTime.toEpochMilli();
    }

    @Override
    public String toString() {
        return "AgentCountStatistics{" +
                "agentCount=" + agentCount +
                ", timestamp=" + timestamp +
                '}';
    }
}
