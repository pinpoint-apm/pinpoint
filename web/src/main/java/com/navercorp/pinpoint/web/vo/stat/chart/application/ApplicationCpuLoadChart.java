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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDoubleFieldBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinCpuLoadBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

/**
 * @author minwoo.jung
 */
public class ApplicationCpuLoadChart extends DefaultApplicationChart<AggreJoinCpuLoadBo, Double> {

    private static final Point.UncollectedPointCreator<ApplicationStatPoint<Double>> UNCOLLECTED_POINT
            = new DoubleApplicationStatPoint.UncollectedCreator(JoinCpuLoadBo.UNCOLLECTED_VALUE);

    private static final ChartGroupBuilder<AggreJoinCpuLoadBo, ApplicationStatPoint<Double>> BUILDER = newChartBuilder();

    public enum CpuLoadChartType implements StatChartGroup.ApplicationChartType {
        CPU_LOAD_JVM,
        CPU_LOAD_SYSTEM
    }

    static ChartGroupBuilder<AggreJoinCpuLoadBo, ApplicationStatPoint<Double>> newChartBuilder() {
        ChartGroupBuilder<AggreJoinCpuLoadBo, ApplicationStatPoint<Double>> builder = new ChartGroupBuilder<>(UNCOLLECTED_POINT);
        builder.addPointFunction(CpuLoadChartType.CPU_LOAD_JVM, ApplicationCpuLoadChart::newJvmCpu);
        builder.addPointFunction(CpuLoadChartType.CPU_LOAD_SYSTEM, ApplicationCpuLoadChart::newSystemCpu);
        return builder;
    }

    public ApplicationCpuLoadChart(TimeWindow timeWindow, List<AggreJoinCpuLoadBo> appStatList) {
        super(timeWindow, appStatList, BUILDER);
    }

    private static ApplicationStatPoint<Double> newSystemCpu(AggreJoinCpuLoadBo statBo) {
        JoinDoubleFieldBo point = statBo.getSystemCpuLoadJoinValue();
        long timestamp = statBo.getTimestamp();
        return StatPointUtils.toDoubleStatPoint(timestamp, point);
    }

    private static ApplicationStatPoint<Double> newJvmCpu(AggreJoinCpuLoadBo statBo) {
        JoinDoubleFieldBo point = statBo.getJvmCpuLoadJoinValue();
        long timestamp = statBo.getTimestamp();
        return StatPointUtils.toDoubleStatPoint(timestamp, point);
    }

}
