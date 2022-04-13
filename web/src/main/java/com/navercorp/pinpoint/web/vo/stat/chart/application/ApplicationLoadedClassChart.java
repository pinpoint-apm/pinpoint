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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinLoadedClassBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

public class ApplicationLoadedClassChart extends DefaultApplicationChart<AggreJoinLoadedClassBo, Long> {

    private static final Point.UncollectedPointCreator<ApplicationStatPoint<Long>> UNCOLLECTED_POINT
            = new LongApplicationStatPoint.UncollectedCreator(JoinLoadedClassBo.UNCOLLECTED_VALUE);

    private static final ChartGroupBuilder<AggreJoinLoadedClassBo, ApplicationStatPoint<Long>> BUILDER = newChartBuilder();



    public enum LoadedClassChartType implements StatChartGroup.ApplicationChartType {
        LOADED_CLASS_COUNT,
        UNLOADED_CLASS_COUNT
    }

    static ChartGroupBuilder<AggreJoinLoadedClassBo, ApplicationStatPoint<Long>> newChartBuilder() {
        ChartGroupBuilder<AggreJoinLoadedClassBo, ApplicationStatPoint<Long>> builder = new ChartGroupBuilder<>(UNCOLLECTED_POINT);
        builder.addPointFunction(LoadedClassChartType.LOADED_CLASS_COUNT, ApplicationLoadedClassChart::newLoadedClassCount);
        builder.addPointFunction(LoadedClassChartType.UNLOADED_CLASS_COUNT, ApplicationLoadedClassChart::newUnloadedClassCount);
        return builder;
    }

    public ApplicationLoadedClassChart(TimeWindow timeWindow, List<AggreJoinLoadedClassBo> appStatList) {
        super(timeWindow, appStatList, BUILDER);
    }

    private static ApplicationStatPoint<Long> newLoadedClassCount(AggreJoinLoadedClassBo loadedClassBo) {
        final JoinLongFieldBo loadedClassJoinValue = loadedClassBo.getLoadedClassJoinValue();
        long timestamp = loadedClassBo.getTimestamp();
        return StatPointUtils.toLongStatPoint(timestamp, loadedClassJoinValue);
    }

    private static ApplicationStatPoint<Long> newUnloadedClassCount(AggreJoinLoadedClassBo loadedClassBo) {
        final JoinLongFieldBo unloadedClassJoinValue = loadedClassBo.getUnloadedClassJoinValue();
        long timestamp = loadedClassBo.getTimestamp();

        return StatPointUtils.toLongStatPoint(timestamp, unloadedClassJoinValue);
    }

}
