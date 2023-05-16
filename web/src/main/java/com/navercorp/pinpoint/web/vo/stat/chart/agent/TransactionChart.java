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
import com.navercorp.pinpoint.web.vo.stat.SampledTransaction;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class TransactionChart extends DefaultAgentChart<SampledTransaction, Double> {

    public enum TransactionChartType implements StatChartGroup.AgentChartType {
        TPS_SAMPLED_NEW,
        TPS_SAMPLED_CONTINUATION,
        TPS_UNSAMPLED_NEW,
        TPS_UNSAMPLED_CONTINUATION,
        TPS_SKIPPED_NEW,
        TPS_SKIPPED_CONTINUATION,
        TPS_TOTAL
    }

    private static final ChartGroupBuilder<SampledTransaction, AgentStatPoint<Double>> BUILDER = newChartBuilder();

    static ChartGroupBuilder<SampledTransaction, AgentStatPoint<Double>> newChartBuilder() {
        ChartGroupBuilder<SampledTransaction, AgentStatPoint<Double>> builder = new ChartGroupBuilder<>(SampledTransaction.UNCOLLECTED_POINT_CREATOR);
        builder.addPointFunction(TransactionChartType.TPS_SAMPLED_NEW, SampledTransaction::getSampledNew);
        builder.addPointFunction(TransactionChartType.TPS_SAMPLED_CONTINUATION, SampledTransaction::getSampledContinuation);
        builder.addPointFunction(TransactionChartType.TPS_UNSAMPLED_NEW, SampledTransaction::getUnsampledNew);
        builder.addPointFunction(TransactionChartType.TPS_UNSAMPLED_CONTINUATION, SampledTransaction::getUnsampledContinuation);
        builder.addPointFunction(TransactionChartType.TPS_SKIPPED_NEW, SampledTransaction::getSkippedNew);
        builder.addPointFunction(TransactionChartType.TPS_SKIPPED_CONTINUATION, SampledTransaction::getSkippedContinuation);
        builder.addPointFunction(TransactionChartType.TPS_TOTAL, SampledTransaction::getTotal);

        return builder;
    }

    public TransactionChart(TimeWindow timeWindow, List<SampledTransaction> statList) {
        super(timeWindow, statList, BUILDER);
    }
}
