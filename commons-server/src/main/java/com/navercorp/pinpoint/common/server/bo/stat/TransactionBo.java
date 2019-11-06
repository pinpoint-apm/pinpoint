/*
 * Copyright 2016 Naver Corp.
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
 * @author HyunGil Jeong
 */
public class TransactionBo implements AgentStatDataPoint {

    public static final long UNCOLLECTED_VALUE = -1;

    private String agentId;
    private long startTimestamp;
    private long timestamp;
    private long collectInterval = UNCOLLECTED_VALUE;
    private long sampledNewCount = UNCOLLECTED_VALUE;
    private long sampledContinuationCount = UNCOLLECTED_VALUE;
    private long unsampledNewCount = UNCOLLECTED_VALUE;
    private long unsampledContinuationCount = UNCOLLECTED_VALUE;
    private long skippedNewSkipCount = UNCOLLECTED_VALUE;
    private long skippedContinuationCount = UNCOLLECTED_VALUE;

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
        return AgentStatType.TRANSACTION;
    }

    public long getCollectInterval() {
        return collectInterval;
    }

    public void setCollectInterval(long collectInterval) {
        this.collectInterval = collectInterval;
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

    public long getSkippedNewSkipCount() {
        return skippedNewSkipCount;
    }

    public void setSkippedNewSkipCount(long skippedNewSkipCount) {
        this.skippedNewSkipCount = skippedNewSkipCount;
    }

    public long getSkippedContinuationCount() {
        return skippedContinuationCount;
    }

    public void setSkippedContinuationCount(long skippedContinuationCount) {
        this.skippedContinuationCount = skippedContinuationCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionBo that = (TransactionBo) o;

        if (startTimestamp != that.startTimestamp) return false;
        if (timestamp != that.timestamp) return false;
        if (collectInterval != that.collectInterval) return false;
        if (sampledNewCount != that.sampledNewCount) return false;
        if (sampledContinuationCount != that.sampledContinuationCount) return false;
        if (unsampledNewCount != that.unsampledNewCount) return false;
        if (unsampledContinuationCount != that.unsampledContinuationCount) return false;
        if (skippedNewSkipCount != that.skippedNewSkipCount) return false;
        if (skippedContinuationCount != that.skippedContinuationCount) return false;
        return agentId != null ? agentId.equals(that.agentId) : that.agentId == null;

    }

    @Override
    public int hashCode() {
        int result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) (collectInterval ^ (collectInterval >>> 32));
        result = 31 * result + (int) (sampledNewCount ^ (sampledNewCount >>> 32));
        result = 31 * result + (int) (sampledContinuationCount ^ (sampledContinuationCount >>> 32));
        result = 31 * result + (int) (unsampledNewCount ^ (unsampledNewCount >>> 32));
        result = 31 * result + (int) (unsampledContinuationCount ^ (unsampledContinuationCount >>> 32));
        result = 31 * result + (int) (skippedNewSkipCount ^ (skippedNewSkipCount >>> 32));
        result = 31 * result + (int) (skippedContinuationCount ^ (skippedContinuationCount >>> 32));
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransactionBo{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", startTimestamp=").append(startTimestamp);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", collectInterval=").append(collectInterval);
        sb.append(", sampledNewCount=").append(sampledNewCount);
        sb.append(", sampledContinuationCount=").append(sampledContinuationCount);
        sb.append(", unsampledNewCount=").append(unsampledNewCount);
        sb.append(", unsampledContinuationCount=").append(unsampledContinuationCount);
        sb.append(", skippedNewSkipCount=").append(skippedNewSkipCount);
        sb.append(", skippedContinuationCount=").append(skippedContinuationCount);
        sb.append('}');
        return sb.toString();
    }
}
