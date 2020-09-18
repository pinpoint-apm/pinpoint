/*
 * Copyright 2020 NAVER Corp.
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

/**
 * @author Hyunjoon Cho
 */
public class ContainerBo implements AgentStatDataPoint{

    public static final long UNCOLLECTED_VALUE = -1;

    private String agentId;
    private long startTimestamp;
    private long timestamp;

    private long userCpuUsage = UNCOLLECTED_VALUE;;
    private long systemCpuUsage = UNCOLLECTED_VALUE;;
    private long memoryUsage = UNCOLLECTED_VALUE;;

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
        return AgentStatType.CONTAINER;
    }

    public long getUserCpuUsage() {
        return userCpuUsage;
    }

    public void setUserCpuUsage(long userCpuUsage) {
        this.userCpuUsage = userCpuUsage;
    }

    public long getSystemCpuUsage() {
        return systemCpuUsage;
    }

    public void setSystemCpuUsage(long systemCpuUsage) {
        this.systemCpuUsage = systemCpuUsage;
    }

    public long getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContainerBo containerBo = (ContainerBo) o;

        if (startTimestamp != containerBo.startTimestamp) return false;
        if (timestamp != containerBo.timestamp) return false;
        if (userCpuUsage != containerBo.userCpuUsage) return false;
        if (systemCpuUsage != containerBo.systemCpuUsage) return false;
        if (memoryUsage != containerBo.memoryUsage) return false;
        return agentId != null ? agentId.equals(containerBo.agentId) : containerBo.agentId == null;

    }

    @Override
    public int hashCode() {
        int result;
        result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) (userCpuUsage ^ (userCpuUsage >>> 32));
        result = 31 * result + (int) (systemCpuUsage ^ (systemCpuUsage >>> 32));
        result = 31 * result + (int) (memoryUsage ^ (memoryUsage >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ContainerBo{" +
                "agentId='" + agentId + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", timestamp=" + timestamp +
                ", userCpuUsage=" + userCpuUsage +
                ", systemCpuUsage=" + systemCpuUsage +
                ", memoryUsage=" + memoryUsage +
                '}';
    }
}
