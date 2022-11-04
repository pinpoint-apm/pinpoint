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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDirectBufferBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

/**
 * @author Roy Kim
 */
public class ApplicationDirectBufferChart extends DefaultApplicationChart<AggreJoinDirectBufferBo, LongApplicationStatPoint> {

    private static final Point.UncollectedPointCreator<LongApplicationStatPoint> UNCOLLECTED_POINT
            = new LongApplicationStatPoint.UncollectedCreator(JoinDirectBufferBo.UNCOLLECTED_VALUE);

    private static final ChartGroupBuilder<AggreJoinDirectBufferBo, LongApplicationStatPoint> BUILDER = newChartBuilder();

    public enum DirectBufferChartType implements StatChartGroup.ApplicationChartType {
        DIRECT_COUNT,
        DIRECT_MEMORY_USED,
        MAPPED_COUNT,
        MAPPED_MEMORY_USED
    }

    static ChartGroupBuilder<AggreJoinDirectBufferBo, LongApplicationStatPoint> newChartBuilder() {
        ChartGroupBuilder<AggreJoinDirectBufferBo, LongApplicationStatPoint> builder = new ChartGroupBuilder<>(UNCOLLECTED_POINT);
        builder.addPointFunction(DirectBufferChartType.DIRECT_COUNT, ApplicationDirectBufferChart::newDirectCount);
        builder.addPointFunction(DirectBufferChartType.DIRECT_MEMORY_USED, ApplicationDirectBufferChart::newDirectMemoryUsed);
        builder.addPointFunction(DirectBufferChartType.MAPPED_COUNT, ApplicationDirectBufferChart::newMappedCount);
        builder.addPointFunction(DirectBufferChartType.MAPPED_MEMORY_USED, ApplicationDirectBufferChart::newMappedMemoryUsed);
        return builder;
    }

    public ApplicationDirectBufferChart(TimeWindow timeWindow, List<AggreJoinDirectBufferBo> appStatList) {
        super(timeWindow, appStatList, BUILDER);
    }

    private static LongApplicationStatPoint newDirectCount(AggreJoinDirectBufferBo directBuffer) {
        final JoinLongFieldBo directCountJoinValue = directBuffer.getDirectCountJoinValue();
        long timestamp = directBuffer.getTimestamp();
        return StatPointUtils.toLongStatPoint(timestamp, directCountJoinValue);
    }

    private static LongApplicationStatPoint newDirectMemoryUsed(AggreJoinDirectBufferBo directBuffer) {
        final JoinLongFieldBo directMemoryUsedJoinValue = directBuffer.getDirectMemoryUsedJoinValue();
        long timestamp = directBuffer.getTimestamp();
        return StatPointUtils.toLongStatPoint(timestamp, directMemoryUsedJoinValue);
    }

    private static LongApplicationStatPoint newMappedCount(AggreJoinDirectBufferBo directBuffer) {
        final JoinLongFieldBo mappedCountJoinValue = directBuffer.getMappedCountJoinValue();
        long timestamp = directBuffer.getTimestamp();
        return StatPointUtils.toLongStatPoint(timestamp, mappedCountJoinValue);
    }

    private static LongApplicationStatPoint newMappedMemoryUsed(AggreJoinDirectBufferBo directBuffer) {
        final JoinLongFieldBo mappedMemoryUsedJoinValue = directBuffer.getMappedMemoryUsedJoinValue();
        long timestamp = directBuffer.getTimestamp();
        return StatPointUtils.toLongStatPoint(timestamp, mappedMemoryUsedJoinValue);
    }

}
