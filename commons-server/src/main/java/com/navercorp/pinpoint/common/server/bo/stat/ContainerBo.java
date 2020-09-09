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

    public static final double UNCOLLECTED_PERCENT_USAGE = -1D;
    public static final long UNCOLLECTED_MEMORY = -1L;

    private String agentId;
    private long startTimestamp;
    private long timestamp;

    private double userCpuUsage = UNCOLLECTED_PERCENT_USAGE;
    private double systemCpuUsage = UNCOLLECTED_PERCENT_USAGE;
    private long memoryMax = UNCOLLECTED_MEMORY;
    private long memoryUsage = UNCOLLECTED_MEMORY;

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

    public double getUserCpuUsage() {
        return userCpuUsage;
    }

    public void setUserCpuUsage(double userCpuUsage) {
        this.userCpuUsage = userCpuUsage;
    }

    public double getSystemCpuUsage() {
        return systemCpuUsage;
    }

    public void setSystemCpuUsage(double systemCpuUsage) {
        this.systemCpuUsage = systemCpuUsage;
    }

    public long getMemoryMax() {
        return memoryMax;
    }

    public void setMemoryMax(long memoryMax) {
        this.memoryMax = memoryMax;
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
        if (memoryMax != containerBo.memoryMax) return false;
        if (memoryUsage != containerBo.memoryUsage) return false;
        return agentId != null ? agentId.equals(containerBo.agentId) : containerBo.agentId == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        temp = Double.doubleToLongBits(userCpuUsage);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(systemCpuUsage);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
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
                ", memoryMax=" + memoryMax +
                ", memoryUsage=" + memoryUsage +
                '}';
    }
}
