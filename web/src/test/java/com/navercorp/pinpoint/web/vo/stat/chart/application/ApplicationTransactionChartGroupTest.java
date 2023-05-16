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
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTransactionBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author minwoo.jung
 */
public class ApplicationTransactionChartGroupTest {

    @Test
    public void createApplicationTransactionChartGroupTest() {
        long time = 1495418083250L;
        Range range = Range.between(time - 240000, time);
        TimeWindow timeWindow = new TimeWindow(range);

        final String id = "test_app";
        List<AggreJoinTransactionBo> aggreJoinTransactionBoList = List.of(
                new AggreJoinTransactionBo(id, 5000, 150, 10, "app_1_1", 230, "app_1_2", time),
                new AggreJoinTransactionBo(id, 5000, 110, 22, "app_2_1", 330, "app_2_2", time - 60000),
                new AggreJoinTransactionBo(id, 5000, 120, 24, "app_3_1", 540, "app_3_2", time - 120000),
                new AggreJoinTransactionBo(id, 5000, 130, 25, "app_4_1", 560, "app_4_2", time - 180000),
                new AggreJoinTransactionBo(id, 5000, 140, 12, "app_5_1", 260, "app_5_2", time - 240000)
        );

        ChartGroupBuilder<AggreJoinTransactionBo, ApplicationStatPoint<Double>> builder = ApplicationTransactionChart.newChartBuilder();
        StatChartGroup<ApplicationStatPoint<Double>> statChartGroup = builder.build(timeWindow, aggreJoinTransactionBoList);
        Map<StatChartGroup.ChartType, Chart<ApplicationStatPoint<Double>>> charts = statChartGroup.getCharts();

        Chart<ApplicationStatPoint<Double>> tranCountChart = charts.get(ApplicationTransactionChart.TransactionChartType.TRANSACTION_COUNT);
        List<ApplicationStatPoint<Double>> tranCountPointList = tranCountChart.getPoints();
        assertThat(tranCountPointList).hasSize(5);
        int index = tranCountPointList.size();
        for (ApplicationStatPoint<Double> point : tranCountPointList) {
            testTranCount(point, aggreJoinTransactionBoList.get(--index));
        }

    }

    private void testTranCount(ApplicationStatPoint<Double> transactionPoint, AggreJoinTransactionBo aggreJoinTransactionBo) {
        final JoinLongFieldBo totalCountJoinValue = aggreJoinTransactionBo.getTotalCountJoinValue();
        assertEquals(transactionPoint.getYValForAvg(), totalCountJoinValue.getAvg(), 0);
        assertEquals(transactionPoint.getYValForMin(), totalCountJoinValue.getMin(), 0);
        assertEquals(transactionPoint.getYValForMax(), totalCountJoinValue.getMax(), 0);
        assertEquals(transactionPoint.getAgentIdForMin(), totalCountJoinValue.getMinAgentId());
        assertEquals(transactionPoint.getAgentIdForMax(), totalCountJoinValue.getMaxAgentId());
    }
}