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

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinFileDescriptorBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Roy Kim
 */
public class ApplicationFileDescriptorChartGroupTest {

    @Test
    public void createApplicationFileDescriptorChartGroupTest() {
        long time = 1495418083250L;
        Range range = new Range(time - 240000, time);
        TimeWindow timeWindow = new TimeWindow(range);

        List<AggreJoinFileDescriptorBo> aggreFileDescriptorList = new ArrayList<>(5);
        AggreJoinFileDescriptorBo aggreJoinFileDescriptorBo1 = new AggreJoinFileDescriptorBo("testApp", 11, 60, "agent1_1", 20, "agent1_2", time);
        AggreJoinFileDescriptorBo aggreJoinFileDescriptorBo2 = new AggreJoinFileDescriptorBo("testApp", 22, 52, "agent2_1", 10, "agent2_2", time - 60000);
        AggreJoinFileDescriptorBo aggreJoinFileDescriptorBo3 = new AggreJoinFileDescriptorBo("testApp", 33, 39, "agent3_1", 9, "agent3_2", time - 120000);
        AggreJoinFileDescriptorBo aggreJoinFileDescriptorBo4 = new AggreJoinFileDescriptorBo("testApp", 44, 42, "agent4_1", 25, "agent4_2", time - 180000);
        AggreJoinFileDescriptorBo aggreJoinFileDescriptorBo5 = new AggreJoinFileDescriptorBo("testApp", 55, 55, "agent5_1", 54, "agent5_2", time - 240000);
        aggreFileDescriptorList.add(aggreJoinFileDescriptorBo1);
        aggreFileDescriptorList.add(aggreJoinFileDescriptorBo2);
        aggreFileDescriptorList.add(aggreJoinFileDescriptorBo3);
        aggreFileDescriptorList.add(aggreJoinFileDescriptorBo4);
        aggreFileDescriptorList.add(aggreJoinFileDescriptorBo5);

        StatChartGroup applicationFileDescriptorChartGroup = new ApplicationFileDescriptorChart.ApplicationFileDescriptorChartGroup(timeWindow, aggreFileDescriptorList);
        Map<StatChartGroup.ChartType, Chart<? extends Point>> charts = applicationFileDescriptorChartGroup.getCharts();
        assertEquals(1, charts.size());

        Chart fileDescriptorChart = charts.get(ApplicationFileDescriptorChart.ApplicationFileDescriptorChartGroup.FileDescriptorChartType.OPEN_FILE_DESCRIPTOR_COUNT);
        List<Point> fileDescriptorPoints = fileDescriptorChart.getPoints();
        assertEquals(5, fileDescriptorPoints.size());
        int index = fileDescriptorPoints.size();

        for (Point point : fileDescriptorPoints) {
            testOpenFileDescriptor((FileDescriptorPoint)point, aggreFileDescriptorList.get(--index));
        }
    }

    private void testOpenFileDescriptor(FileDescriptorPoint fileDescriptorPoint, AggreJoinFileDescriptorBo aggreJoinFileDescriptorBo) {
        assertEquals(fileDescriptorPoint.getXVal(), aggreJoinFileDescriptorBo.getTimestamp());
        assertEquals(fileDescriptorPoint.getYValForAvg(), aggreJoinFileDescriptorBo.getAvgOpenFDCount(), 0);
        assertEquals(fileDescriptorPoint.getYValForMin(), aggreJoinFileDescriptorBo.getMinOpenFDCount(), 0);
        assertEquals(fileDescriptorPoint.getYValForMax(), aggreJoinFileDescriptorBo.getMaxOpenFDCount(), 0);
        assertEquals(fileDescriptorPoint.getAgentIdForMin(), aggreJoinFileDescriptorBo.getMinOpenFDCountAgentId());
        assertEquals(fileDescriptorPoint.getAgentIdForMax(), aggreJoinFileDescriptorBo.getMaxOpenFDCountAgentId());
    }
}