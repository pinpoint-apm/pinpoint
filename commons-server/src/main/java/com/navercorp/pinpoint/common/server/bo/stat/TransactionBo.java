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

    private final long collectInterval;
    private final long sampledNewCount;
    private final long sampledContinuationCount;
    private final long unsampledNewCount;
    private final long unsampledContinuationCount;
    private final long skippedNewSkipCount;
    private final long skippedContinuationCount;

    public TransactionBo(DataPoint point,
                         long collectInterval,
                         long sampledNewCount, long sampledContinuationCount,
                         long unsampledNewCount, long unsampledContinuationCount,
                         long skippedNewSkipCount, long skippedContinuationCount) {
        super(point);
        this.collectInterval = collectInterval;
        this.sampledNewCount = sampledNewCount;
        this.sampledContinuationCount = sampledContinuationCount;
        this.unsampledNewCount = unsampledNewCount;
        this.unsampledContinuationCount = unsampledContinuationCount;
        this.skippedNewSkipCount = skippedNewSkipCount;
        this.skippedContinuationCount = skippedContinuationCount;
    }


    @Override
    public AgentStatType getAgentStatType() {
        return AgentStatType.TRANSACTION;
    }

    public long getCollectInterval() {
        return collectInterval;
    }

    public long getSampledNewCount() {
        return sampledNewCount;
    }

    public long getSampledContinuationCount() {
        return sampledContinuationCount;
    }

    public long getUnsampledNewCount() {
        return unsampledNewCount;
    }

    public long getUnsampledContinuationCount() {
        return unsampledContinuationCount;
    }

    public long getSkippedNewSkipCount() {
        return skippedNewSkipCount;
    }

    public long getSkippedContinuationCount() {
        return skippedContinuationCount;
    }

    @Override
    public String toString() {
        return "TransactionBo{" +
                "point=" + point +
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
