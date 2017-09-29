/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.vo.stat.chart;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinCpuLoadBo;
import com.navercorp.pinpoint.web.vo.stat.chart.CpuLoadPoint.UncollectedCpuLoadPointCreater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author minwoo.jung
 */
public class ApplicationCpuLoadChartGroup implements ApplicationStatChartGroup {

    private static final UncollectedCpuLoadPointCreater UNCOLLECTED_CPULOADPOINT = new UncollectedCpuLoadPointCreater();

    private final Map<ChartType, Chart> cpuLoadChartMap;

    public enum CpuLoadChartType implements ChartType {
        CPU_LOAD_JVM,
        CPU_LOAD_SYSTEM
    }

    public ApplicationCpuLoadChartGroup(TimeWindow timeWindow, List<AggreJoinCpuLoadBo> AggreCpuLoadList) {
        cpuLoadChartMap = new HashMap<>();
        List<Point> jvmCpuLoadList = new ArrayList<>(AggreCpuLoadList.size());
        List<Point> systemCpuLoadList = new ArrayList<>(AggreCpuLoadList.size());

        for (AggreJoinCpuLoadBo aggreJoinCpuLoadBo : AggreCpuLoadList) {
            jvmCpuLoadList.add(new CpuLoadPoint(aggreJoinCpuLoadBo.getTimestamp(), aggreJoinCpuLoadBo.getMinJvmCpuLoad(), aggreJoinCpuLoadBo.getMinJvmCpuAgentId(), aggreJoinCpuLoadBo.getMaxJvmCpuLoad(), aggreJoinCpuLoadBo.getMaxJvmCpuAgentId(), aggreJoinCpuLoadBo.getJvmCpuLoad()));
            systemCpuLoadList.add(new CpuLoadPoint(aggreJoinCpuLoadBo.getTimestamp(), aggreJoinCpuLoadBo.getMinSystemCpuLoad(), aggreJoinCpuLoadBo.getMinSysCpuAgentId(), aggreJoinCpuLoadBo.getMaxSystemCpuLoad(), aggreJoinCpuLoadBo.getMaxSysCpuAgentId(), aggreJoinCpuLoadBo.getSystemCpuLoad()));
        }

        cpuLoadChartMap.put(CpuLoadChartType.CPU_LOAD_JVM, new TimeSeriesChartBuilder(timeWindow, UNCOLLECTED_CPULOADPOINT).build(jvmCpuLoadList));
        cpuLoadChartMap.put(CpuLoadChartType.CPU_LOAD_SYSTEM, new TimeSeriesChartBuilder(timeWindow, UNCOLLECTED_CPULOADPOINT).build(systemCpuLoadList));
    }

    @Override
    public Map<ChartType, Chart> getCharts() {
        return this.cpuLoadChartMap;
    }
}
