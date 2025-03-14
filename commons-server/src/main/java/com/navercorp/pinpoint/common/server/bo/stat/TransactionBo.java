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
public class TransactionBo extends AbstractStatDataPoint {

    public static final long UNCOLLECTED_VALUE = -1;

    private long collectInterval = UNCOLLECTED_VALUE;
    private long sampledNewCount = UNCOLLECTED_VALUE;
    private long sampledContinuationCount = UNCOLLECTED_VALUE;
    private long unsampledNewCount = UNCOLLECTED_VALUE;
    private long unsampledContinuationCount = UNCOLLECTED_VALUE;
    private long skippedNewSkipCount = UNCOLLECTED_VALUE;
    private long skippedContinuationCount = UNCOLLECTED_VALUE;

    public TransactionBo(DataPoint point) {
        super(point);
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
    public String toString() {
        return "TransactionBo{" +
                ", point=" + point +
                ", collectInterval=" + collectInterval +
                ", sampledNewCount=" + sampledNewCount +
                ", sampledContinuationCount=" + sampledContinuationCount +
                ", unsampledNewCount=" + unsampledNewCount +
                ", unsampledContinuationCount=" + unsampledContinuationCount +
                ", skippedNewSkipCount=" + skippedNewSkipCount +
                ", skippedContinuationCount=" + skippedContinuationCount +
                '}';
    }
}
