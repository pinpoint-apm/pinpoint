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

import com.google.common.collect.ImmutableMap;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.SampledTransaction;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
            TPS_SKIPPED_NEW,
            TPS_SKIPPED_CONTINUATION,
            TPS_TOTAL
        }

        private TransactionChartGroup(TimeWindow timeWindow, List<SampledTransaction> sampledTransactions) {
            this.timeWindow = timeWindow;
            this.transactionCharts = newChart(sampledTransactions);
        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<SampledTransaction> sampledTransactions) {
            Chart<AgentStatPoint<Double>> sampledNewTps = newChart(sampledTransactions, SampledTransaction::getSampledNew);
            Chart<AgentStatPoint<Double>> sampledContinuationTps = newChart(sampledTransactions, SampledTransaction::getSampledContinuation);
            Chart<AgentStatPoint<Double>> unsampledNewTps = newChart(sampledTransactions, SampledTransaction::getUnsampledNew);
            Chart<AgentStatPoint<Double>> unsampledContinuationTps = newChart(sampledTransactions, SampledTransaction::getUnsampledContinuation);
            Chart<AgentStatPoint<Double>> skippedNewTps = newChart(sampledTransactions, SampledTransaction::getSkippedNew);
            Chart<AgentStatPoint<Double>> skippedContinuationTps = newChart(sampledTransactions, SampledTransaction::getSkippedContinuation);
            Chart<AgentStatPoint<Double>> totalTps = newChart(sampledTransactions, SampledTransaction::getTotal);

            ImmutableMap.Builder<ChartType, Chart<? extends Point>> builder = ImmutableMap.builder();
            builder.put(TransactionChartType.TPS_SAMPLED_NEW, sampledNewTps);
            builder.put(TransactionChartType.TPS_SAMPLED_CONTINUATION, sampledContinuationTps);
            builder.put(TransactionChartType.TPS_UNSAMPLED_NEW, unsampledNewTps);
            builder.put(TransactionChartType.TPS_UNSAMPLED_CONTINUATION, unsampledContinuationTps);
            builder.put(TransactionChartType.TPS_SKIPPED_NEW, skippedNewTps);
            builder.put(TransactionChartType.TPS_SKIPPED_CONTINUATION, skippedContinuationTps);
            builder.put(TransactionChartType.TPS_TOTAL, totalTps);

            return builder.build();
        }

        private Chart<AgentStatPoint<Double>> newChart(List<SampledTransaction> transactionList, Function<SampledTransaction, AgentStatPoint<Double>> filter) {
            TimeSeriesChartBuilder<AgentStatPoint<Double>> builder = new TimeSeriesChartBuilder<>(timeWindow, SampledTransaction.UNCOLLECTED_POINT_CREATOR);
            return builder.build(transactionList, filter);
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
