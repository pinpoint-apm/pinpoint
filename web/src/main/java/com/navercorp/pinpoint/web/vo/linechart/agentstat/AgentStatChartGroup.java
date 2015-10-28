/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo.linechart.agentstat;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.AgentStat;
import com.navercorp.pinpoint.web.vo.linechart.Chart;
import com.navercorp.pinpoint.web.vo.linechart.DataPoint;
import com.navercorp.pinpoint.web.vo.linechart.SampledTimeSeriesDoubleChartBuilder;
import com.navercorp.pinpoint.web.vo.linechart.SampledTimeSeriesLongChartBuilder;
import com.navercorp.pinpoint.web.vo.linechart.Chart.ChartBuilder;

/**
 * @author harebox
 * @author HyunGil Jeong
 */
public class AgentStatChartGroup {

    private static enum ChartType {
        JVM_MEMORY_HEAP_USED,
        JVM_MEMORY_HEAP_MAX,
        JVM_MEMORY_NON_HEAP_USED,
        JVM_MEMORY_NON_HEAP_MAX,
        JVM_GC_OLD_COUNT,
        JVM_GC_OLD_TIME,
        CPU_LOAD_JVM,
        CPU_LOAD_SYSTEM,
        TPS_SAMPLED_NEW,
        TPS_SAMPLED_CONTINUATION,
        TPS_UNSAMPLED_NEW,
        TPS_UNSAMPLED_CONTINUATION,
        TPS_TOTAL
    }

    private static final int UNCOLLECTED_DATA = AgentStat.NOT_COLLECTED;

    private String type;

    private final Map<ChartType, ChartBuilder<? extends Number, ? extends Number>> chartBuilders;

    private final Map<ChartType, Chart> charts;

    public AgentStatChartGroup(TimeWindow timeWindow) {
        this.chartBuilders = new EnumMap<ChartType, ChartBuilder<? extends Number, ? extends Number>>(ChartType.class);
        this.chartBuilders.put(ChartType.JVM_MEMORY_HEAP_USED, new SampledTimeSeriesLongChartBuilder(timeWindow, UNCOLLECTED_DATA));
        this.chartBuilders.put(ChartType.JVM_MEMORY_HEAP_MAX, new SampledTimeSeriesLongChartBuilder(timeWindow, UNCOLLECTED_DATA));
        this.chartBuilders.put(ChartType.JVM_MEMORY_NON_HEAP_USED, new SampledTimeSeriesLongChartBuilder(timeWindow, UNCOLLECTED_DATA));
        this.chartBuilders.put(ChartType.JVM_MEMORY_NON_HEAP_MAX, new SampledTimeSeriesLongChartBuilder(timeWindow, UNCOLLECTED_DATA));
        this.chartBuilders.put(ChartType.JVM_GC_OLD_COUNT, new SampledTimeSeriesLongChartBuilder(timeWindow, UNCOLLECTED_DATA));
        this.chartBuilders.put(ChartType.JVM_GC_OLD_TIME, new SampledTimeSeriesLongChartBuilder(timeWindow, UNCOLLECTED_DATA));
        this.chartBuilders.put(ChartType.CPU_LOAD_JVM, new SampledTimeSeriesDoubleChartBuilder(timeWindow, UNCOLLECTED_DATA));
        this.chartBuilders.put(ChartType.CPU_LOAD_SYSTEM, new SampledTimeSeriesDoubleChartBuilder(timeWindow, UNCOLLECTED_DATA));
        this.chartBuilders.put(ChartType.TPS_SAMPLED_NEW, new SampledTimeSeriesDoubleChartBuilder(timeWindow, UNCOLLECTED_DATA, 1));
        this.chartBuilders.put(ChartType.TPS_SAMPLED_CONTINUATION, new SampledTimeSeriesDoubleChartBuilder(timeWindow, UNCOLLECTED_DATA, 1));
        this.chartBuilders.put(ChartType.TPS_UNSAMPLED_NEW, new SampledTimeSeriesDoubleChartBuilder(timeWindow, UNCOLLECTED_DATA, 1));
        this.chartBuilders.put(ChartType.TPS_UNSAMPLED_CONTINUATION, new SampledTimeSeriesDoubleChartBuilder(timeWindow, UNCOLLECTED_DATA, 1));
        this.chartBuilders.put(ChartType.TPS_TOTAL, new SampledTimeSeriesDoubleChartBuilder(timeWindow, UNCOLLECTED_DATA, 1));
        this.charts = new EnumMap<ChartType, Chart>(ChartType.class);
    }

    public void addAgentStats(List<AgentStat> agentStats) {
        for (AgentStat agentStat : agentStats) {
            if (agentStat != null) {
                addMemoryGcData(agentStat);
                addCpuLoadData(agentStat);
                addTransactionData(agentStat);
            }
        }
    }

    public void buildCharts() {
        for (ChartType chartType : ChartType.values()) {
            this.charts.put(chartType, this.chartBuilders.get(chartType).buildChart());
        }
    }

    private void addMemoryGcData(AgentStat agentStat) {
        this.type = agentStat.getGcType();
        long timestamp = agentStat.getTimestamp();
        ((SampledTimeSeriesLongChartBuilder) this.chartBuilders.get(ChartType.JVM_MEMORY_HEAP_USED)).addDataPoint(new DataPoint<Long, Long>(timestamp, agentStat.getHeapUsed()));
        ((SampledTimeSeriesLongChartBuilder) this.chartBuilders.get(ChartType.JVM_MEMORY_HEAP_MAX)).addDataPoint(new DataPoint<Long, Long>(timestamp, agentStat.getHeapMax()));
        ((SampledTimeSeriesLongChartBuilder) this.chartBuilders.get(ChartType.JVM_MEMORY_NON_HEAP_USED)).addDataPoint(new DataPoint<Long, Long>(timestamp, agentStat.getNonHeapUsed()));
        ((SampledTimeSeriesLongChartBuilder) this.chartBuilders.get(ChartType.JVM_MEMORY_NON_HEAP_MAX)).addDataPoint(new DataPoint<Long, Long>(timestamp, agentStat.getNonHeapMax()));
        ((SampledTimeSeriesLongChartBuilder) this.chartBuilders.get(ChartType.JVM_GC_OLD_COUNT)).addDataPoint(new DataPoint<Long, Long>(timestamp, agentStat.getGcOldCount()));
        ((SampledTimeSeriesLongChartBuilder) this.chartBuilders.get(ChartType.JVM_GC_OLD_TIME)).addDataPoint(new DataPoint<Long, Long>(timestamp, agentStat.getGcOldTime()));
    }

    private void addCpuLoadData(AgentStat agentStat) {
        long timestamp = agentStat.getTimestamp();
        double jvmCpuUsagePercentage = agentStat.getJvmCpuUsage() * 100;
        double systemCpuUsagePercentage = agentStat.getSystemCpuUsage() * 100;
        ((SampledTimeSeriesDoubleChartBuilder) this.chartBuilders.get(ChartType.CPU_LOAD_JVM)).addDataPoint(new DataPoint<Long, Double>(timestamp, jvmCpuUsagePercentage));
        ((SampledTimeSeriesDoubleChartBuilder) this.chartBuilders.get(ChartType.CPU_LOAD_SYSTEM)).addDataPoint(new DataPoint<Long, Double>(timestamp, systemCpuUsagePercentage));
    }

    private void addTransactionData(AgentStat agentStat) {
        long timestamp = agentStat.getTimestamp();
        long interval = agentStat.getCollectInterval();
        if (interval > 0) {
            double sampledNewTps = calculateTps(agentStat.getSampledNewCount(), interval);
            double sampledContinuationTps = calculateTps(agentStat.getSampledContinuationCount(), interval);
            double unsampledNewTps = calculateTps(agentStat.getUnsampledNewCount(), interval);
            double unsampledContinuationTps = calculateTps(agentStat.getUnsampledContinuationCount(), interval);
            double totalTps = sampledNewTps + sampledContinuationTps + unsampledNewTps + unsampledContinuationTps;
            ((SampledTimeSeriesDoubleChartBuilder) this.chartBuilders.get(ChartType.TPS_SAMPLED_NEW)).addDataPoint(new DataPoint<Long, Double>(timestamp, sampledNewTps));
            ((SampledTimeSeriesDoubleChartBuilder) this.chartBuilders.get(ChartType.TPS_SAMPLED_CONTINUATION)).addDataPoint(new DataPoint<Long, Double>(timestamp, sampledContinuationTps));
            ((SampledTimeSeriesDoubleChartBuilder) this.chartBuilders.get(ChartType.TPS_UNSAMPLED_NEW)).addDataPoint(new DataPoint<Long, Double>(timestamp, unsampledNewTps));
            ((SampledTimeSeriesDoubleChartBuilder) this.chartBuilders.get(ChartType.TPS_UNSAMPLED_CONTINUATION)).addDataPoint(new DataPoint<Long, Double>(timestamp, unsampledContinuationTps));
            ((SampledTimeSeriesDoubleChartBuilder) this.chartBuilders.get(ChartType.TPS_TOTAL)).addDataPoint(new DataPoint<Long, Double>(timestamp, totalTps));
        }
    }

    private double calculateTps(long count, long intervalMs) {
        final int numDecimal = 1;
        if (count == UNCOLLECTED_DATA) {
            return UNCOLLECTED_DATA;
        }
        return new BigDecimal(count / (intervalMs / 1000D)).setScale(numDecimal, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public String getType() {
        return type;
    }

    public Map<ChartType, Chart> getCharts() {
        return charts;
    }
}