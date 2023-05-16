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
import com.navercorp.pinpoint.web.vo.stat.AggreJoinFileDescriptorBo;
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
public class ApplicationFileDescriptorChartGroupTest {

    @Test
    public void createApplicationFileDescriptorChartGroupTest() {
        long time = 1495418083250L;
        Range range = Range.between(time - 240000, time);
        TimeWindow timeWindow = new TimeWindow(range);

        List<AggreJoinFileDescriptorBo> aggreFileDescriptorList = List.of(
                new AggreJoinFileDescriptorBo("testApp", 11, 60, "agent1_1", 20, "agent1_2", time),
                new AggreJoinFileDescriptorBo("testApp", 22, 52, "agent2_1", 10, "agent2_2", time - 60000),
                new AggreJoinFileDescriptorBo("testApp", 33, 39, "agent3_1", 9, "agent3_2", time - 120000),
                new AggreJoinFileDescriptorBo("testApp", 44, 42, "agent4_1", 25, "agent4_2", time - 180000),
                new AggreJoinFileDescriptorBo("testApp", 55, 55, "agent5_1", 54, "agent5_2", time - 240000)
        );

        ChartGroupBuilder<AggreJoinFileDescriptorBo, ApplicationStatPoint<Long>> builder = ApplicationFileDescriptorChart.newChartBuilder();
        StatChartGroup<ApplicationStatPoint<Long>> applicationFileDescriptorChartGroup = builder.build(timeWindow, aggreFileDescriptorList);
        Map<StatChartGroup.ChartType, Chart<ApplicationStatPoint<Long>>> charts = applicationFileDescriptorChartGroup.getCharts();
        assertThat(charts).hasSize(1);

        Chart<ApplicationStatPoint<Long>> fileDescriptorChart = charts.get(ApplicationFileDescriptorChart.FileDescriptorChartType.OPEN_FILE_DESCRIPTOR_COUNT);
        List<ApplicationStatPoint<Long>> fileDescriptorPoints = fileDescriptorChart.getPoints();
        assertThat(fileDescriptorPoints).hasSize(5);
        int index = fileDescriptorPoints.size();

        for (ApplicationStatPoint<Long> point : fileDescriptorPoints) {
            testOpenFileDescriptor(point, aggreFileDescriptorList.get(--index));
        }
    }

    private void testOpenFileDescriptor(ApplicationStatPoint<Long> fileDescriptorPoint, AggreJoinFileDescriptorBo aggreJoinFileDescriptorBo) {
        assertEquals(fileDescriptorPoint.getXVal(), aggreJoinFileDescriptorBo.getTimestamp());
        final JoinLongFieldBo openFdCountJoinValue = aggreJoinFileDescriptorBo.getOpenFdCountJoinValue();
        assertEquals(fileDescriptorPoint.getYValForAvg(), openFdCountJoinValue.getAvg(), 0);
        assertEquals(fileDescriptorPoint.getYValForMin(), openFdCountJoinValue.getMin(), 0);
        assertEquals(fileDescriptorPoint.getYValForMax(), openFdCountJoinValue.getMax(), 0);
        assertEquals(fileDescriptorPoint.getAgentIdForMin(), openFdCountJoinValue.getMinAgentId());
        assertEquals(fileDescriptorPoint.getAgentIdForMax(), openFdCountJoinValue.getMaxAgentId());
    }
}