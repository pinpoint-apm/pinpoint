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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTransactionBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTransactionBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.apache.commons.math3.util.Precision;

import java.util.List;

/**
 * @author minwoo.jung
 */
public class ApplicationTransactionChart extends DefaultApplicationChart<AggreJoinTransactionBo, Double> {

    private static final Point.UncollectedPointCreator<ApplicationStatPoint<Double>> UNCOLLECTED_POINT
            = new DoubleApplicationStatPoint.UncollectedCreator(JoinTransactionBo.UNCOLLECTED_VALUE);

    public enum TransactionChartType implements StatChartGroup.ApplicationChartType {
        TRANSACTION_COUNT
    }

    private static final ChartGroupBuilder<AggreJoinTransactionBo, ApplicationStatPoint<Double>> BUILDER = newChartBuilder();

    static ChartGroupBuilder<AggreJoinTransactionBo, ApplicationStatPoint<Double>> newChartBuilder() {
        ChartGroupBuilder<AggreJoinTransactionBo, ApplicationStatPoint<Double>> builder = new ChartGroupBuilder<>(UNCOLLECTED_POINT);
        builder.addPointFunction(TransactionChartType.TRANSACTION_COUNT, ApplicationTransactionChart::newTransactionPoint);
        return builder;
    }

    public ApplicationTransactionChart(TimeWindow timeWindow, List<AggreJoinTransactionBo> appStatList) {
        super(timeWindow, appStatList, BUILDER);
    }

    private static ApplicationStatPoint<Double> newTransactionPoint(AggreJoinTransactionBo transaction) {
        final JoinLongFieldBo totalCountJoinValue = transaction.getTotalCountJoinValue();
        double minTotalCount = calculateTPS(totalCountJoinValue.getMin(), transaction.getCollectInterval());
        double maxTotalCount = calculateTPS(totalCountJoinValue.getMax(), transaction.getCollectInterval());
        double totalCount = calculateTPS(totalCountJoinValue.getAvg(), transaction.getCollectInterval());
        return new DoubleApplicationStatPoint(transaction.getTimestamp(), minTotalCount, totalCountJoinValue.getMinAgentId(), maxTotalCount, totalCountJoinValue.getMaxAgentId(), totalCount);
    }

    private static double calculateTPS(double value, long timeMs) {
        if (value <= 0) {
            return value;
        }

        return Precision.round(value / (timeMs / 1000D), 1);
    }

}
