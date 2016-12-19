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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.SlotType;

/**
 * @author HyunGil Jeong
 */
@Deprecated
public class AgentStat {
    public static final long AGGR_SAMPLE_INTERVAL = TimeUnit.MINUTES.toMillis(10);
    public static final long RAW_SAMPLE_INTERVAL = TimeUnit.SECONDS.toMillis(5);
    public static final int NOT_COLLECTED = -1;

    private final String agentId;
    private final long timestamp;

    private long collectInterval;

    private String gcType;
    private long gcOldCount = NOT_COLLECTED;
    private long gcOldTime = NOT_COLLECTED;
    private long heapUsed = NOT_COLLECTED;
    private long heapMax = NOT_COLLECTED;
    private long nonHeapUsed = NOT_COLLECTED;
    private long nonHeapMax = NOT_COLLECTED;

    private double jvmCpuUsage = NOT_COLLECTED;
    private double systemCpuUsage = NOT_COLLECTED;

    private long sampledNewCount = NOT_COLLECTED;
    private long sampledContinuationCount = NOT_COLLECTED;
    private long unsampledNewCount = NOT_COLLECTED;
    private long unsampledContinuationCount = NOT_COLLECTED;

    private HistogramSchema histogramSchema;
    private Map<SlotType, Integer> activeTraceCounts;

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

    public long getCollectInterval() {
        return this.collectInterval;
    }

    public void setCollectInterval(long collectInterval) {
        this.collectInterval = collectInterval;
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

    public long getSampledNewCount() {
        return sampledNewCount;
    }

    public void setSampledNewCount(long sampledNewCount) {
        this.sampledNewCount = sampledNewCount;
    }

    public long getSampledContinuationCount() {
        return sampledContinuationCount;
    }

    public void setSampledContinuationCount(long sampledContinuationCount) {
        this.sampledContinuationCount = sampledContinuationCount;
    }

    public long getUnsampledNewCount() {
        return unsampledNewCount;
    }

    public void setUnsampledNewCount(long unsampledNewCount) {
        this.unsampledNewCount = unsampledNewCount;
    }

    public long getUnsampledContinuationCount() {
        return unsampledContinuationCount;
    }

    public void setUnsampledContinuationCount(long unsampledContinuationCount) {
        this.unsampledContinuationCount = unsampledContinuationCount;
    }

    public HistogramSchema getHistogramSchema() {
        return histogramSchema;
    }

    public void setHistogramSchema(HistogramSchema histogramSchema) {
        this.histogramSchema = histogramSchema;
    }

    public Map<SlotType, Integer> getActiveTraceCounts() {
        return activeTraceCounts;
    }

    public void setActiveTraceCounts(Map<SlotType, Integer> activeTraceCounts) {
        this.activeTraceCounts = activeTraceCounts;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentStat{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", collectInterval=").append(collectInterval);
        sb.append(", gcType='").append(gcType).append('\'');
        sb.append(", gcOldCount=").append(gcOldCount);
        sb.append(", gcOldTime=").append(gcOldTime);
        sb.append(", heapUsed=").append(heapUsed);
        sb.append(", heapMax=").append(heapMax);
        sb.append(", nonHeapUsed=").append(nonHeapUsed);
        sb.append(", nonHeapMax=").append(nonHeapMax);
        sb.append(", jvmCpuUsage=").append(jvmCpuUsage);
        sb.append(", systemCpuUsage=").append(systemCpuUsage);
        sb.append(", sampledNewCount=").append(sampledNewCount);
        sb.append(", sampledContinuationCount=").append(sampledContinuationCount);
        sb.append(", unsampledNewCount=").append(unsampledNewCount);
        sb.append(", unsampledContinuationCount=").append(unsampledContinuationCount);
        if (histogramSchema != null) {
            sb.append(", histogramSchemaTypeCode=" + histogramSchema.getTypeCode());
        }
        if (activeTraceCounts != null) {
            sb.append(", activeTraceCounts=" + activeTraceCounts);
        }
        sb.append('}');
        return sb.toString();
    }
}
