/*
 * Copyright 2014 NAVER Corp.
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

/**
 * @author HyunGil Jeong
 */
public class AgentStat {
    
    public static final int NOT_COLLECTED = -1;
    
    private final String agentId;
    private final long timestamp;

    private String gcType;
    private long gcOldCount = NOT_COLLECTED;
    private long gcOldTime = NOT_COLLECTED;
    private long heapUsed = NOT_COLLECTED;
    private long heapMax = NOT_COLLECTED;
    private long nonHeapUsed = NOT_COLLECTED;
    private long nonHeapMax = NOT_COLLECTED;
    private double jvmCpuUsage = NOT_COLLECTED;
    private double systemCpuUsage = NOT_COLLECTED;
    private int tps = NOT_COLLECTED;
    
    public AgentStat(String agentId, long timestamp) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (timestamp < 0) {
            throw new NullPointerException("timestamp must not be negative");
        }
        this.agentId = agentId;
        this.timestamp = timestamp;
    }
    
    public String getAgentId() {
        return this.agentId;
    }
    
    public long getTimestamp() {
        return this.timestamp;
    }

    public String getGcType() {
        return gcType;
    }

    public void setGcType(String gcType) {
        this.gcType = gcType;
    }

    public long getGcOldCount() {
        return gcOldCount;
    }

    public void setGcOldCount(long gcOldCount) {
        this.gcOldCount = gcOldCount;
    }

    public long getGcOldTime() {
        return gcOldTime;
    }

    public void setGcOldTime(long gcOldTime) {
        this.gcOldTime = gcOldTime;
    }

    public long getHeapUsed() {
        return heapUsed;
    }

    public void setHeapUsed(long heapUsed) {
        this.heapUsed = heapUsed;
    }

    public long getHeapMax() {
        return heapMax;
    }

    public void setHeapMax(long heapMax) {
        this.heapMax = heapMax;
    }

    public long getNonHeapUsed() {
        return nonHeapUsed;
    }

    public void setNonHeapUsed(long nonHeapUsed) {
        this.nonHeapUsed = nonHeapUsed;
    }

    public long getNonHeapMax() {
        return nonHeapMax;
    }

    public void setNonHeapMax(long nonHeapMax) {
        this.nonHeapMax = nonHeapMax;
    }

    public double getJvmCpuUsage() {
        return jvmCpuUsage;
    }

    public void setJvmCpuUsage(double jvmCpuUsage) {
        this.jvmCpuUsage = jvmCpuUsage;
    }

    public double getSystemCpuUsage() {
        return systemCpuUsage;
    }

    public void setSystemCpuUsage(double systemCpuUsage) {
        this.systemCpuUsage = systemCpuUsage;
    }

    public int getTps() {
        return tps;
    }

    public void setTps(int tps) {
        this.tps = tps;
    }

    @Override
    public String toString() {
        return "AgentStat [agentId=" + agentId + ", timestamp=" + timestamp + ", tps=" + tps + "]";
    }

}
