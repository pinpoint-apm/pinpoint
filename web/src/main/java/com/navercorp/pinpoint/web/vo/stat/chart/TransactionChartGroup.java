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

package com.navercorp.pinpoint.web.vo.stat.chart;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.SampledTransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class TransactionChartGroup implements AgentStatChartGroup {

    private static final Double UNCOLLECTED_TPS = -1D;

    private final Map<ChartType, Chart> transactionCharts;

    public enum TransactionChartType implements ChartType {
        TPS_SAMPLED_NEW,
        TPS_SAMPLED_CONTINUATION,
        TPS_UNSAMPLED_NEW,
        TPS_UNSAMPLED_CONTINUATION,
        TPS_TOTAL
    }

    public TransactionChartGroup(TimeWindow timeWindow, List<SampledTransaction> sampledTransactions) {
        this.transactionCharts = new HashMap<>();
        List<Point<Long, Double>> sampledNewTps = new ArrayList<>(sampledTransactions.size());
        List<Point<Long, Double>> sampledContinuationTps = new ArrayList<>(sampledTransactions.size());
        List<Point<Long, Double>> unsampledNewTps = new ArrayList<>(sampledTransactions.size());
        List<Point<Long, Double>> unsampledContinuationTps = new ArrayList<>(sampledTransactions.size());
        List<Point<Long, Double>> totalTps = new ArrayList<>();
        for (SampledTransaction sampledTransaction : sampledTransactions) {
            sampledNewTps.add(sampledTransaction.getSampledNew());
            sampledContinuationTps.add(sampledTransaction.getSampledContinuation());
            unsampledNewTps.add(sampledTransaction.getUnsampledNew());
            unsampledContinuationTps.add(sampledTransaction.getUnsampledContinuation());
            totalTps.add(sampledTransaction.getTotal());
        }
        transactionCharts.put(TransactionChartType.TPS_SAMPLED_NEW, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_TPS).build(sampledNewTps));
        transactionCharts.put(TransactionChartType.TPS_SAMPLED_CONTINUATION, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_TPS).build(sampledContinuationTps));
        transactionCharts.put(TransactionChartType.TPS_UNSAMPLED_NEW, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_TPS).build(unsampledNewTps));
        transactionCharts.put(TransactionChartType.TPS_UNSAMPLED_CONTINUATION, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_TPS).build(unsampledContinuationTps));
        transactionCharts.put(TransactionChartType.TPS_TOTAL, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_TPS).build(totalTps));
    }

    @Override
    public Map<ChartType, Chart> getCharts() {
        return this.transactionCharts;
    }
}
