/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.stat;

import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class DeadlockBo implements AgentWarningStatDataPoint {

    public static final int UNCOLLECTED_INT_VALUE = -1;

    private String agentId;
    private long startTimestamp;
    private long timestamp;
    private int deadlockedThreadCount = UNCOLLECTED_INT_VALUE;
    private List<TThreadDump> threadDumpList;

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public long getStartTimestamp() {
        return startTimestamp;
    }

    @Override
    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public AgentStatType getAgentStatType() {
        return AgentStatType.DEADLOCK;
    }

    public int getDeadlockedThreadCount() {
        return deadlockedThreadCount;
    }

    public void setDeadlockedThreadCount(int deadlockedThreadCount) {
        this.deadlockedThreadCount = deadlockedThreadCount;
    }

    public List<TThreadDump> getThreadDumpList() {
        return threadDumpList;
    }

    public void setThreadDumpList(List<TThreadDump> threadDumpList) {
        this.threadDumpList = threadDumpList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeadlockBo{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", startTimestamp=").append(startTimestamp);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", deadlockedThreadCount=").append(deadlockedThreadCount);
        sb.append(", threadDumpList=").append(threadDumpList);
        sb.append('}');
        return sb.toString();
    }

}
