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
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.UncollectedPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import com.navercorp.pinpoint.web.vo.stat.SampledTransaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Component
public class TransactionSampler implements AgentStatSampler<TransactionBo, SampledTransaction> {

    private static final double UNCOLLECTED_TPS = -1D;
    private static final int NUM_DECIMAL_PLACES = 1;
    public static final DownSampler<Double> DOUBLE_DOWN_SAMPLER = DownSamplers.getDoubleDownSampler(UNCOLLECTED_TPS, NUM_DECIMAL_PLACES);

    @Override
    public SampledTransaction sampleDataPoints(int timeWindowIndex, long timestamp, List<TransactionBo> dataPoints, TransactionBo previousDataPoint) {
        List<Double> sampledNews = new ArrayList<>(dataPoints.size());
        List<Double> sampledContinuations = new ArrayList<>(dataPoints.size());
        List<Double> unsampledNews = new ArrayList<>(dataPoints.size());
        List<Double> unsampledContinuations = new ArrayList<>(dataPoints.size());
        List<Double> totals = new ArrayList<>(dataPoints.size());
        for (TransactionBo transactionBo : dataPoints) {
            long collectInterval = transactionBo.getCollectInterval();
            if (collectInterval > 0) {
                boolean isTransactionCollected = false;
                long totalCount = 0;
                if (transactionBo.getSampledNewCount() != TransactionBo.UNCOLLECTED_VALUE) {
                    isTransactionCollected = true;
                    long sampledNewCount = transactionBo.getSampledNewCount();
                    sampledNews.add(calculateTps(sampledNewCount, collectInterval));
                    totalCount += sampledNewCount;
                }
                if (transactionBo.getSampledContinuationCount() != TransactionBo.UNCOLLECTED_VALUE) {
                    isTransactionCollected = true;
                    long sampledContinuationCount = transactionBo.getSampledContinuationCount();
                    sampledContinuations.add(calculateTps(sampledContinuationCount, collectInterval));
                    totalCount += sampledContinuationCount;
                }
                if (transactionBo.getUnsampledNewCount() != TransactionBo.UNCOLLECTED_VALUE) {
                    isTransactionCollected = true;
                    long unsampledNewCount = transactionBo.getUnsampledNewCount();
                    unsampledNews.add(calculateTps(unsampledNewCount, collectInterval));
                    totalCount += unsampledNewCount;
                }
                if (transactionBo.getUnsampledContinuationCount() != TransactionBo.UNCOLLECTED_VALUE) {
                    isTransactionCollected = true;
                    long unsampledContinuationCount = transactionBo.getUnsampledContinuationCount();
                    unsampledContinuations.add(calculateTps(unsampledContinuationCount, collectInterval));
                    totalCount += unsampledContinuationCount;
                }
                if (isTransactionCollected) {
                    totals.add(calculateTps(totalCount, collectInterval));
                }
            }
        }
        SampledTransaction sampledTransaction = new SampledTransaction();
        sampledTransaction.setSampledNew(createPoint(timestamp, sampledNews));
        sampledTransaction.setSampledContinuation(createPoint(timestamp, sampledContinuations));
        sampledTransaction.setUnsampledNew(createPoint(timestamp, unsampledNews));
        sampledTransaction.setUnsampledContinuation(createPoint(timestamp, unsampledContinuations));
        sampledTransaction.setTotal(createPoint(timestamp, totals));
        return sampledTransaction;
    }

    private double calculateTps(long count, long intervalMs) {
        return AgentStatUtils.calculateRate(count, intervalMs, NUM_DECIMAL_PLACES, UNCOLLECTED_TPS);
    }

    private Point<Long, Double> createPoint(long timestamp, List<Double> values) {
        if (values.isEmpty()) {
            return new UncollectedPoint<>(timestamp, UNCOLLECTED_TPS);
        } else {
            return new Point<>(
                    timestamp,
                    DOUBLE_DOWN_SAMPLER.sampleMin(values),
                    DOUBLE_DOWN_SAMPLER.sampleMax(values),
                    DOUBLE_DOWN_SAMPLER.sampleAvg(values),
                    DOUBLE_DOWN_SAMPLER.sampleSum(values));
        }
    }
}
