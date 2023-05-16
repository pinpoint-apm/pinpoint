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

package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledFileDescriptor;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

/**
 * @author Roy Kim
 */
public class FileDescriptorChart extends DefaultAgentChart<SampledFileDescriptor, Long> {

    public enum FileDescriptorChartType implements StatChartGroup.AgentChartType {
        OPEN_FILE_DESCRIPTOR_COUNT
    }

    private static final ChartGroupBuilder<SampledFileDescriptor, AgentStatPoint<Long>> BUILDER = newChartBuilder();

    static ChartGroupBuilder<SampledFileDescriptor, AgentStatPoint<Long>> newChartBuilder() {
        ChartGroupBuilder<SampledFileDescriptor, AgentStatPoint<Long>> builder = new ChartGroupBuilder<>(SampledFileDescriptor.UNCOLLECTED_POINT_CREATOR);
        builder.addPointFunction(FileDescriptorChartType.OPEN_FILE_DESCRIPTOR_COUNT, SampledFileDescriptor::getOpenFileDescriptorCount);
        return builder;
    }

    public FileDescriptorChart(TimeWindow timeWindow, List<SampledFileDescriptor> statList) {
        super(timeWindow, statList, BUILDER);
    }
}
