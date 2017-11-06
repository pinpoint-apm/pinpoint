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

package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTransactionBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class ApplicationTransactionChart implements StatChart {

    private final ApplicationTransactionChartGroup applicationTransactionChartGroup;

    public ApplicationTransactionChart(TimeWindow timeWindow, List<AggreJoinTransactionBo> aggreJoinTransactionBoList) {
        this.applicationTransactionChartGroup = new ApplicationTransactionChartGroup(timeWindow, aggreJoinTransactionBoList);
    }

    @Override
    public StatChartGroup getCharts() {
        return applicationTransactionChartGroup;
    }

    public static class ApplicationTransactionChartGroup implements StatChartGroup {

        private static final TransactionPoint.UncollectedTransactionPointCreator UNCOLLECTED_TRANSACTION_POINT = new TransactionPoint.UncollectedTransactionPointCreator();

        private final TimeWindow timeWindow;
        private final Map<ChartType, Chart<? extends Point>> transactionChartMap;

        public enum TransactionChartType implements ApplicationChartType {
            TRANSACTION_COUNT
        }

        public ApplicationTransactionChartGroup(TimeWindow timeWindow, List<AggreJoinTransactionBo> aggreJoinTransactionBoList) {
            this.timeWindow = timeWindow;
            transactionChartMap = new HashMap<>();
            List<TransactionPoint> transactionList = new ArrayList<>(aggreJoinTransactionBoList.size());

            for (AggreJoinTransactionBo aggreJoinTransactionBo : aggreJoinTransactionBoList) {
                double minTotalCount = calculateTPS(aggreJoinTransactionBo.getMinTotalCount(), aggreJoinTransactionBo.getCollectInterval());
                double maxTotalCount = calculateTPS(aggreJoinTransactionBo.getMaxTotalCount(), aggreJoinTransactionBo.getCollectInterval());
                double totalCount = calculateTPS(aggreJoinTransactionBo.getTotalCount(), aggreJoinTransactionBo.getCollectInterval());
                transactionList.add(new TransactionPoint(aggreJoinTransactionBo.getTimestamp(), minTotalCount, aggreJoinTransactionBo.getMinTotalCountAgentId(), maxTotalCount, aggreJoinTransactionBo.getMaxTotalCountAgentId(), totalCount));
            }
            TimeSeriesChartBuilder<TransactionPoint> chartBuilder = new TimeSeriesChartBuilder<>(this.timeWindow, UNCOLLECTED_TRANSACTION_POINT);
            transactionChartMap.put(TransactionChartType.TRANSACTION_COUNT, chartBuilder.build(transactionList));
        }

        private double calculateTPS(double value, long timeMs) {
            if (value <= 0) {
                return value;
            }

            return BigDecimal.valueOf(value / (timeMs / 1000D)).setScale(1, RoundingMode.HALF_UP).doubleValue();
        }

        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return this.transactionChartMap;
        }
    }
}
