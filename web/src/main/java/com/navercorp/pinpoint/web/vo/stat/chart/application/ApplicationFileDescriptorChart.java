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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinFileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinFileDescriptorBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

/**
 * @author Roy Kim
 */
public class ApplicationFileDescriptorChart extends DefaultApplicationChart<AggreJoinFileDescriptorBo, Long> {

    private static final Point.UncollectedPointCreator<ApplicationStatPoint<Long>> UNCOLLECTED_POINT
            = new LongApplicationStatPoint.UncollectedCreator(JoinFileDescriptorBo.UNCOLLECTED_VALUE);

    private static final ChartGroupBuilder<AggreJoinFileDescriptorBo, ApplicationStatPoint<Long>> BUILDER = newChartBuilder();

    public enum FileDescriptorChartType implements StatChartGroup.ApplicationChartType {
        OPEN_FILE_DESCRIPTOR_COUNT
    }

    static ChartGroupBuilder<AggreJoinFileDescriptorBo, ApplicationStatPoint<Long>> newChartBuilder() {
        ChartGroupBuilder<AggreJoinFileDescriptorBo, ApplicationStatPoint<Long>> builder = new ChartGroupBuilder<>(UNCOLLECTED_POINT);
        builder.addPointFunction(FileDescriptorChartType.OPEN_FILE_DESCRIPTOR_COUNT, ApplicationFileDescriptorChart::newOpenFileDescriptorCount);
        return builder;
    }

    public ApplicationFileDescriptorChart(TimeWindow timeWindow, List<AggreJoinFileDescriptorBo> appStatList) {
        super(timeWindow, appStatList, BUILDER);
    }

    private static ApplicationStatPoint<Long> newOpenFileDescriptorCount(AggreJoinFileDescriptorBo fileDescriptor) {
        final JoinLongFieldBo openFdCountJoinValue = fileDescriptor.getOpenFdCountJoinValue();
        long timestamp = fileDescriptor.getTimestamp();
        return StatPointUtils.toLongStatPoint(timestamp, openFdCountJoinValue);
    }

}
