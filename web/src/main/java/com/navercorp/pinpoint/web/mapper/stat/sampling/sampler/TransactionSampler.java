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

package com.navercorp.pinpoint.web.mapper.stat.sampling.sampler;

import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.web.vo.stat.SampledCpuLoad;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import com.navercorp.pinpoint.web.vo.stat.SampledTransaction;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToLongFunction;

/**
 * @author HyunGil Jeong
 */
@Component
public class TransactionSampler implements AgentStatSampler<TransactionBo, SampledTransaction> {

    private static final int NUM_DECIMAL_PLACES = 1;
    private static final DownSampler<Double> DOUBLE_DOWN_SAMPLER = DownSamplers.getDoubleDownSampler(SampledTransaction.UNCOLLECTED_VALUE, NUM_DECIMAL_PLACES);

    @Override
    public SampledTransaction sampleDataPoints(int timeWindowIndex, long timestamp, List<TransactionBo> dataPoints, TransactionBo previousDataPoint) {

        final AgentStatPoint<Double> sampledNew = newAgentStatPoint(timestamp, dataPoints, TransactionBo::getSampledNewCount);
        final AgentStatPoint<Double> sampledContinuation = newAgentStatPoint(timestamp, dataPoints, TransactionBo::getSampledContinuationCount);
        final AgentStatPoint<Double> unsampledNew = newAgentStatPoint(timestamp, dataPoints, TransactionBo::getUnsampledNewCount);
        final AgentStatPoint<Double> unsampledContinuation = newAgentStatPoint(timestamp, dataPoints, TransactionBo::getUnsampledContinuationCount);
        final AgentStatPoint<Double> skippedNew = newAgentStatPoint(timestamp, dataPoints, TransactionBo::getSkippedNewSkipCount);
        final AgentStatPoint<Double> skippedContinuation = newAgentStatPoint(timestamp, dataPoints, TransactionBo::getSkippedContinuationCount);

        final List<Double> totals = calculateTotalTps(dataPoints);
        AgentStatPoint<Double> total = createPoint(timestamp, totals);

        SampledTransaction sampledTransaction = new SampledTransaction(sampledNew, sampledContinuation, unsampledNew, unsampledContinuation, skippedNew, skippedContinuation, total);
        return sampledTransaction;
    }

    private AgentStatPoint<Double> newAgentStatPoint(long timestamp, List<TransactionBo> dataPoints, ToLongFunction<TransactionBo> function) {
        final List<Double> sampledNews = calculateTps(dataPoints, function);
        return createPoint(timestamp, sampledNews);
    }

    private List<Double> calculateTotalTps(List<TransactionBo> dataPoints) {
        final List<Double> result = new ArrayList<>(dataPoints.size());
        for (TransactionBo transactionBo : dataPoints) {
            final Double total = getTotalTps(transactionBo);
            if (total != null) {
                result.add(total);
            }
        }
        return result;
    }

    private Double getTotalTps(TransactionBo transactionBo) {
        final long collectInterval = transactionBo.getCollectInterval();
        if (collectInterval > 0) {
            boolean isTransactionCollected = false;
            long totalCount = 0;
            final long sampledNewCount = transactionBo.getSampledNewCount();
            if (sampledNewCount != TransactionBo.UNCOLLECTED_VALUE) {
                isTransactionCollected = true;
                totalCount += sampledNewCount;
            }
            final long sampledContinuationCount = transactionBo.getSampledContinuationCount();
            if (sampledContinuationCount != TransactionBo.UNCOLLECTED_VALUE) {
                isTransactionCollected = true;
                totalCount += sampledContinuationCount;
            }
            final long unsampledNewCount = transactionBo.getUnsampledNewCount();
            if (unsampledNewCount != TransactionBo.UNCOLLECTED_VALUE) {
                isTransactionCollected = true;
                totalCount += unsampledNewCount;
            }
            final long unsampledContinuationCount = transactionBo.getUnsampledContinuationCount();
            if (unsampledContinuationCount != TransactionBo.UNCOLLECTED_VALUE) {
                isTransactionCollected = true;
                totalCount += unsampledContinuationCount;
            }
            final long skippedNewCount = transactionBo.getSkippedNewSkipCount();
            if (skippedNewCount != TransactionBo.UNCOLLECTED_VALUE) {
                isTransactionCollected = true;
                totalCount += skippedNewCount;
            }
            final long skippedContinuationCount = transactionBo.getSkippedContinuationCount();
            if (skippedContinuationCount != TransactionBo.UNCOLLECTED_VALUE) {
                isTransactionCollected = true;
                totalCount += skippedContinuationCount;
            }
            if (isTransactionCollected) {
                return calculateTps(totalCount, collectInterval);
            }
        }
        return null;
    }

    private List<Double> calculateTps(List<TransactionBo> dataPoints, ToLongFunction<TransactionBo> function) {
        final List<Double> result = new ArrayList<>(dataPoints.size());
        for (TransactionBo transactionBo : dataPoints) {
            final long collectInterval = transactionBo.getCollectInterval();
            if (collectInterval > 0) {
                final long count = function.applyAsLong(transactionBo);
                if (count != TransactionBo.UNCOLLECTED_VALUE) {
                    final double tps = calculateTps(count, collectInterval);
                    result.add(tps);
                }
            }
        }
        return result;
    }

    private double calculateTps(long count, long intervalMs) {
        return AgentStatUtils.calculateRate(count, intervalMs, NUM_DECIMAL_PLACES, SampledTransaction.UNCOLLECTED_VALUE);
    }

    private AgentStatPoint<Double> createPoint(long timestamp, List<Double> values) {
        if (values.isEmpty()) {
            return SampledCpuLoad.UNCOLLECTED_POINT_CREATOR.createUnCollectedPoint(timestamp);
        }

        return new AgentStatPoint<>(
                    timestamp,
                    DOUBLE_DOWN_SAMPLER.sampleMin(values),
                    DOUBLE_DOWN_SAMPLER.sampleMax(values),
                    DOUBLE_DOWN_SAMPLER.sampleAvg(values),
                    DOUBLE_DOWN_SAMPLER.sampleSum(values));
    }
}
