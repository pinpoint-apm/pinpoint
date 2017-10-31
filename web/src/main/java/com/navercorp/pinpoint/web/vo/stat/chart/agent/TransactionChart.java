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

package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.SampledTransaction;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class TransactionChart implements StatChart {

    private final TransactionChartGroup transactionChartGroup;

    public TransactionChart(TimeWindow timeWindow, List<SampledTransaction> sampledTransactions) {
        this.transactionChartGroup = new TransactionChartGroup(timeWindow, sampledTransactions);
    }

    @Override
    public StatChartGroup getCharts() {
        return transactionChartGroup;
    }

    public static class TransactionChartGroup implements StatChartGroup {

        private final TimeWindow timeWindow;

        private final Map<ChartType, Chart<? extends Point>> transactionCharts;

        public enum TransactionChartType implements AgentChartType {
            TPS_SAMPLED_NEW,
            TPS_SAMPLED_CONTINUATION,
            TPS_UNSAMPLED_NEW,
            TPS_UNSAMPLED_CONTINUATION,
            TPS_TOTAL
        }

        private TransactionChartGroup(TimeWindow timeWindow, List<SampledTransaction> sampledTransactions) {
            this.timeWindow = timeWindow;
            this.transactionCharts = new HashMap<>();
            List<AgentStatPoint<Double>> sampledNewTps = new ArrayList<>(sampledTransactions.size());
            List<AgentStatPoint<Double>> sampledContinuationTps = new ArrayList<>(sampledTransactions.size());
            List<AgentStatPoint<Double>> unsampledNewTps = new ArrayList<>(sampledTransactions.size());
            List<AgentStatPoint<Double>> unsampledContinuationTps = new ArrayList<>(sampledTransactions.size());
            List<AgentStatPoint<Double>> totalTps = new ArrayList<>();
            for (SampledTransaction sampledTransaction : sampledTransactions) {
                sampledNewTps.add(sampledTransaction.getSampledNew());
                sampledContinuationTps.add(sampledTransaction.getSampledContinuation());
                unsampledNewTps.add(sampledTransaction.getUnsampledNew());
                unsampledContinuationTps.add(sampledTransaction.getUnsampledContinuation());
                totalTps.add(sampledTransaction.getTotal());
            }
            TimeSeriesChartBuilder<AgentStatPoint<Double>> chartBuilder = new TimeSeriesChartBuilder<>(this.timeWindow, SampledTransaction.UNCOLLECTED_POINT_CREATER);
            transactionCharts.put(TransactionChartType.TPS_SAMPLED_NEW, chartBuilder.build(sampledNewTps));
            transactionCharts.put(TransactionChartType.TPS_SAMPLED_CONTINUATION, chartBuilder.build(sampledContinuationTps));
            transactionCharts.put(TransactionChartType.TPS_UNSAMPLED_NEW, chartBuilder.build(unsampledNewTps));
            transactionCharts.put(TransactionChartType.TPS_UNSAMPLED_CONTINUATION, chartBuilder.build(unsampledContinuationTps));
            transactionCharts.put(TransactionChartType.TPS_TOTAL, chartBuilder.build(totalTps));
        }

        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return transactionCharts;
        }
    }
}
