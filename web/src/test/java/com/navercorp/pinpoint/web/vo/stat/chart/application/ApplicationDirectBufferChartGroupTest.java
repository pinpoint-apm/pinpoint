/*
 * Copyright 2018 NAVER Corp.
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
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDirectBufferBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Roy Kim
 */
public class ApplicationDirectBufferChartGroupTest {

    @Test
    public void createApplicationDirectBufferChartGroupTest() {
        long time = 1495418083250L;
        Range range = Range.between(time - 240000, time);
        TimeWindow timeWindow = new TimeWindow(range);

        List<AggreJoinDirectBufferBo> aggreDirectBufferList = List.of(
                new AggreJoinDirectBufferBo("testApp", 11, 60, "agent1_1", 20, "agent1_2", 11, 60, "agent1_1", 20, "agent1_2", 11, 60, "agent1_1", 20, "agent1_2", 11, 60, "agent1_1", 20, "agent1_2", time),
                new AggreJoinDirectBufferBo("testApp", 22, 52, "agent2_1", 10, "agent2_2", 22, 52, "agent2_1", 10, "agent2_2", 22, 52, "agent2_1", 10, "agent2_2", 22, 52, "agent2_1", 10, "agent2_2", time - 60000),
                new AggreJoinDirectBufferBo("testApp", 33, 39, "agent3_1", 9, "agent3_2", 33, 39, "agent3_1", 9, "agent3_2", 33, 39, "agent3_1", 9, "agent3_2", 33, 39, "agent3_1", 9, "agent3_2", time - 120000),
                new AggreJoinDirectBufferBo("testApp", 44, 42, "agent4_1", 25, "agent4_2", 44, 42, "agent4_1", 25, "agent4_2", 44, 42, "agent4_1", 25, "agent4_2", 44, 42, "agent4_1", 25, "agent4_2", time - 180000),
                new AggreJoinDirectBufferBo("testApp", 55, 55, "agent5_1", 54, "agent5_2", 55, 55, "agent5_1", 54, "agent5_2", 55, 55, "agent5_1", 54, "agent5_2", 55, 55, "agent5_1", 54, "agent5_2", time - 240000)
        );


        ChartGroupBuilder<AggreJoinDirectBufferBo, ApplicationStatPoint<Long>> builder = ApplicationDirectBufferChart.newChartBuilder();
        StatChartGroup<ApplicationStatPoint<Long>> group = builder.build(timeWindow, aggreDirectBufferList);
        Map<StatChartGroup.ChartType, Chart<ApplicationStatPoint<Long>>> charts = group.getCharts();
        assertThat(charts).hasSize(4);

        Chart<ApplicationStatPoint<Long>> directCountChart = charts.get(ApplicationDirectBufferChart.DirectBufferChartType.DIRECT_COUNT);
        List<ApplicationStatPoint<Long>> directCountPoints = directCountChart.getPoints();
        assertThat(directCountPoints).hasSize(5);
        int index = directCountPoints.size();
        for (ApplicationStatPoint<Long> point : directCountPoints) {
            testDirectCount(point, aggreDirectBufferList.get(--index));
        }

        Chart<ApplicationStatPoint<Long>> directMemoryUsedChart = charts.get(ApplicationDirectBufferChart.DirectBufferChartType.DIRECT_MEMORY_USED);
        List<ApplicationStatPoint<Long>> directMemoryUsedPoints = directMemoryUsedChart.getPoints();
        assertThat(directMemoryUsedPoints).hasSize(5);
        index = directMemoryUsedPoints.size();
        for (ApplicationStatPoint<Long> point : directMemoryUsedPoints) {
            testDirectMemoryUsed(point, aggreDirectBufferList.get(--index));
        }

        Chart<ApplicationStatPoint<Long>> mappedCountChart = charts.get(ApplicationDirectBufferChart.DirectBufferChartType.MAPPED_COUNT);
        List<ApplicationStatPoint<Long>> mappeedCountPoints = mappedCountChart.getPoints();
        assertThat(mappeedCountPoints).hasSize(5);
        index = mappeedCountPoints.size();
        for (ApplicationStatPoint<Long> point : mappeedCountPoints) {
            testMappedCount(point, aggreDirectBufferList.get(--index));
        }

        Chart<ApplicationStatPoint<Long>> mappedMemoryUsedChart = charts.get(ApplicationDirectBufferChart.DirectBufferChartType.MAPPED_MEMORY_USED);
        List<ApplicationStatPoint<Long>> mappedMemoryUsedPoints = mappedMemoryUsedChart.getPoints();
        assertThat(mappedMemoryUsedPoints).hasSize(5);
        index = mappedMemoryUsedPoints.size();
        for (ApplicationStatPoint<Long> point : mappedMemoryUsedPoints) {
            testMappedMemoryUsed(point, aggreDirectBufferList.get(--index));
        }
    }

    private void testDirectCount(ApplicationStatPoint<Long> directBufferPoint, AggreJoinDirectBufferBo aggreJoinDirectBufferBo) {
        final JoinLongFieldBo directCountJoinValue = aggreJoinDirectBufferBo.getDirectCountJoinValue();
        assertEquals(directBufferPoint.getXVal(), aggreJoinDirectBufferBo.getTimestamp());
        assertEquals(directBufferPoint.getYValForAvg(), directCountJoinValue.getAvg(), 0);
        assertEquals(directBufferPoint.getYValForMin(), directCountJoinValue.getMin(), 0);
        assertEquals(directBufferPoint.getYValForMax(), directCountJoinValue.getMax(), 0);
        assertEquals(directBufferPoint.getAgentIdForMin(), directCountJoinValue.getMinAgentId());
        assertEquals(directBufferPoint.getAgentIdForMax(), directCountJoinValue.getMaxAgentId());
    }

    private void testDirectMemoryUsed(ApplicationStatPoint<Long> directBufferPoint, AggreJoinDirectBufferBo aggreJoinDirectBufferBo) {
        final JoinLongFieldBo directMemoryUsedJoinValue = aggreJoinDirectBufferBo.getDirectMemoryUsedJoinValue();
        assertEquals(directBufferPoint.getXVal(), aggreJoinDirectBufferBo.getTimestamp());
        assertEquals(directBufferPoint.getYValForAvg(), directMemoryUsedJoinValue.getAvg(), 0);
        assertEquals(directBufferPoint.getYValForMin(), directMemoryUsedJoinValue.getMin(), 0);
        assertEquals(directBufferPoint.getYValForMax(), directMemoryUsedJoinValue.getMax(), 0);
        assertEquals(directBufferPoint.getAgentIdForMin(), directMemoryUsedJoinValue.getMinAgentId());
        assertEquals(directBufferPoint.getAgentIdForMax(), directMemoryUsedJoinValue.getMaxAgentId());
    }

    private void testMappedCount(ApplicationStatPoint<Long> directBufferPoint, AggreJoinDirectBufferBo aggreJoinDirectBufferBo) {
        final JoinLongFieldBo mappedCountJoinValue = aggreJoinDirectBufferBo.getMappedCountJoinValue();
        assertEquals(directBufferPoint.getXVal(), aggreJoinDirectBufferBo.getTimestamp());
        assertEquals(directBufferPoint.getYValForAvg(), mappedCountJoinValue.getAvg(), 0);
        assertEquals(directBufferPoint.getYValForMin(), mappedCountJoinValue.getMin(), 0);
        assertEquals(directBufferPoint.getYValForMax(), mappedCountJoinValue.getMax(), 0);
        assertEquals(directBufferPoint.getAgentIdForMin(), mappedCountJoinValue.getMinAgentId());
        assertEquals(directBufferPoint.getAgentIdForMax(), mappedCountJoinValue.getMaxAgentId());
    }

    private void testMappedMemoryUsed(ApplicationStatPoint<Long> directBufferPoint, AggreJoinDirectBufferBo aggreJoinDirectBufferBo) {
        final JoinLongFieldBo mappedMemoryUsedJoinValue = aggreJoinDirectBufferBo.getMappedMemoryUsedJoinValue();
        assertEquals(directBufferPoint.getXVal(), aggreJoinDirectBufferBo.getTimestamp());
        assertEquals(directBufferPoint.getYValForAvg(), mappedMemoryUsedJoinValue.getAvg(), 0);
        assertEquals(directBufferPoint.getYValForMin(), mappedMemoryUsedJoinValue.getMin(), 0);
        assertEquals(directBufferPoint.getYValForMax(), mappedMemoryUsedJoinValue.getMax(), 0);
        assertEquals(directBufferPoint.getAgentIdForMin(), mappedMemoryUsedJoinValue.getMinAgentId());
        assertEquals(directBufferPoint.getAgentIdForMax(), mappedMemoryUsedJoinValue.getMaxAgentId());
    }
}