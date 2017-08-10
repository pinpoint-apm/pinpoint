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

package com.navercorp.pinpoint.web.vo.stat.chart;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTransactionBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTransactionBo;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class ApplicationTransactionChartGroupTest {

    @Test
    public void createApplicationTransactionChartGroupTest() {
        long time = 1495418083250L;
        Range range = new Range(time - 240000, time);
        TimeWindow timeWindow = new TimeWindow(range);

        final String id = "test_app";
        List<AggreJoinTransactionBo> aggreJoinTransactionBoList = new ArrayList<AggreJoinTransactionBo>(5);
        AggreJoinTransactionBo aggreJoinTransactionBo1 = new AggreJoinTransactionBo(id, 5000, 150, 10, "app_1_1", 230, "app_1_2", time);
        AggreJoinTransactionBo aggreJoinTransactionBo2 = new AggreJoinTransactionBo(id, 5000, 110, 22, "app_2_1", 330, "app_2_2", time - 60000);
        AggreJoinTransactionBo aggreJoinTransactionBo3 = new AggreJoinTransactionBo(id, 5000, 120, 24, "app_3_1", 540, "app_3_2", time - 120000);
        AggreJoinTransactionBo aggreJoinTransactionBo4 = new AggreJoinTransactionBo(id, 5000, 130, 25, "app_4_1", 560, "app_4_2", time - 180000);
        AggreJoinTransactionBo aggreJoinTransactionBo5 = new AggreJoinTransactionBo(id, 5000, 140, 12, "app_5_1", 260, "app_5_2", time - 240000);
        aggreJoinTransactionBoList.add(aggreJoinTransactionBo1);
        aggreJoinTransactionBoList.add(aggreJoinTransactionBo2);
        aggreJoinTransactionBoList.add(aggreJoinTransactionBo3);
        aggreJoinTransactionBoList.add(aggreJoinTransactionBo4);
        aggreJoinTransactionBoList.add(aggreJoinTransactionBo5);

        ApplicationTransactionChartGroup applicationTransactionChartGroup = new ApplicationTransactionChartGroup(timeWindow, aggreJoinTransactionBoList);
        Map<ApplicationStatChartGroup.ChartType, Chart> charts = applicationTransactionChartGroup.getCharts();

        Chart tranCountChart = charts.get(ApplicationTransactionChartGroup.TransactionChartType.TRANSACTION_COUNT);
        List<Point> tranCountPointList = tranCountChart.getPoints();
        assertEquals(5, tranCountPointList.size());
        int index = tranCountPointList.size();
        for (Point point : tranCountPointList) {
            testTranCount((TransactionPoint) point, aggreJoinTransactionBoList.get(--index));
        }

    }

    private void testTranCount(TransactionPoint transactionPoint, AggreJoinTransactionBo aggreJoinTransactionBo) {
        assertEquals(transactionPoint.getyValForAvg(), calculateTPS(aggreJoinTransactionBo.getTotalCount(), aggreJoinTransactionBo.getCollectInterval()), 0);
        assertEquals(transactionPoint.getyValForMin(), calculateTPS(aggreJoinTransactionBo.getMinTotalCount(), aggreJoinTransactionBo.getCollectInterval()), 0);
        assertEquals(transactionPoint.getyValForMax(), calculateTPS(aggreJoinTransactionBo.getMaxTotalCount(), aggreJoinTransactionBo.getCollectInterval()), 0);
        assertEquals(transactionPoint.getAgentIdForMin(), aggreJoinTransactionBo.getMinTotalCountAgentId());
        assertEquals(transactionPoint.getAgentIdForMax(), aggreJoinTransactionBo.getMaxTotalCountAgentId());
    }

    private double calculateTPS(double value, long timeMs) {
        if (value <= 0) {
            return value;
        }

        return BigDecimal.valueOf(value / (timeMs / 1000D)).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}