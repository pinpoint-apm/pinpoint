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

package com.navercorp.pinpoint.web.mapper.stat;

import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import com.navercorp.pinpoint.web.vo.stat.SampledTransaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class SampledTransactionResultExtractor extends SampledAgentStatResultExtractor<TransactionBo, SampledTransaction> {

    private static final double UNCOLLECTED_TPS = -1D;
    private static final int NUM_DECIMAL_PLACES = 1;
    public static final DownSampler<Double> DOUBLE_DOWN_SAMPLER = DownSamplers.getDoubleDownSampler(UNCOLLECTED_TPS, NUM_DECIMAL_PLACES);

    public SampledTransactionResultExtractor(TimeWindow timeWindow, AgentStatMapper<TransactionBo> rowMapper) {
        super(timeWindow, rowMapper);
    }

    @Override
    protected SampledTransaction sampleCurrentBatch(long timestamp, List<TransactionBo> dataPointsToSample) {
        List<Double> sampledNews = new ArrayList<>(dataPointsToSample.size());
        List<Double> sampledContinuations = new ArrayList<>(dataPointsToSample.size());
        List<Double> unsampledNews = new ArrayList<>(dataPointsToSample.size());
        List<Double> unsampledContinuations = new ArrayList<>(dataPointsToSample.size());
        List<Double> totals = new ArrayList<>(dataPointsToSample.size());
        for (TransactionBo transactionBo : dataPointsToSample) {
            long collectInterval = transactionBo.getCollectInterval();
            long sampledNewCount = transactionBo.getSampledNewCount();
            long sampledContinuationCount = transactionBo.getSampledContinuationCount();
            long unsampledNewCount = transactionBo.getUnsampledNewCount();
            long unsampledContinuationCount = transactionBo.getUnsampledContinuationCount();
            long total = sampledNewCount + sampledContinuationCount + unsampledNewCount + unsampledContinuationCount;
            sampledNews.add(calculateTps(sampledNewCount, collectInterval));
            sampledContinuations.add(calculateTps(sampledContinuationCount, collectInterval));
            unsampledNews.add(calculateTps(unsampledNewCount, collectInterval));
            unsampledContinuations.add(calculateTps(unsampledContinuationCount, collectInterval));
            totals.add(calculateTps(total, collectInterval));
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
        return new Point<>(timestamp, DOUBLE_DOWN_SAMPLER.sampleMin(values), DOUBLE_DOWN_SAMPLER.sampleMax(values), DOUBLE_DOWN_SAMPLER.sampleAvg(values));
    }
}
