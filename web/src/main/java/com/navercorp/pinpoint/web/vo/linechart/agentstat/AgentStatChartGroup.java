package com.nhn.pinpoint.web.vo.linechart.agentstat;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.nhn.pinpoint.common.bo.AgentStatCpuLoadBo;
import com.nhn.pinpoint.common.bo.AgentStatMemoryGcBo;
import com.nhn.pinpoint.web.util.TimeWindow;
import com.nhn.pinpoint.web.vo.AgentStat;
import com.nhn.pinpoint.web.vo.linechart.DataPoint;
import com.nhn.pinpoint.web.vo.linechart.Chart;
import com.nhn.pinpoint.web.vo.linechart.Chart.ChartBuilder;
import com.nhn.pinpoint.web.vo.linechart.SampledTimeSeriesDoubleChartBuilder;
import com.nhn.pinpoint.web.vo.linechart.SampledTimeSeriesLongChartBuilder;

/**
 * @author harebox
 * @author hyungil.jeong
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
        CPU_LOAD_SYSTEM;
    }
    
    private static final int uncollectedData = -1;

    private String type;

    private final Map<ChartType, ChartBuilder<? extends Number, ? extends Number>> chartBuilders;
    
    private final Map<ChartType, Chart> charts;
    
    public AgentStatChartGroup(TimeWindow timeWindow) {
        this.chartBuilders = new EnumMap<ChartType, ChartBuilder<? extends Number, ? extends Number>>(ChartType.class);
        this.chartBuilders.put(ChartType.JVM_MEMORY_HEAP_USED, new SampledTimeSeriesLongChartBuilder(timeWindow, uncollectedData));
        this.chartBuilders.put(ChartType.JVM_MEMORY_HEAP_MAX, new SampledTimeSeriesLongChartBuilder(timeWindow, uncollectedData));
        this.chartBuilders.put(ChartType.JVM_MEMORY_NON_HEAP_USED, new SampledTimeSeriesLongChartBuilder(timeWindow, uncollectedData));
        this.chartBuilders.put(ChartType.JVM_MEMORY_NON_HEAP_MAX, new SampledTimeSeriesLongChartBuilder(timeWindow, uncollectedData));
        this.chartBuilders.put(ChartType.JVM_GC_OLD_COUNT, new SampledTimeSeriesLongChartBuilder(timeWindow, uncollectedData));
        this.chartBuilders.put(ChartType.JVM_GC_OLD_TIME, new SampledTimeSeriesLongChartBuilder(timeWindow, uncollectedData));
        this.chartBuilders.put(ChartType.CPU_LOAD_JVM, new SampledTimeSeriesDoubleChartBuilder(timeWindow, uncollectedData));
        this.chartBuilders.put(ChartType.CPU_LOAD_SYSTEM, new SampledTimeSeriesDoubleChartBuilder(timeWindow, uncollectedData));
        this.charts = new EnumMap<ChartType, Chart>(ChartType.class);
    }

    public void addAgentStats(List<AgentStat> agentStats) {
        for (AgentStat agentStat : agentStats) {
            addMemoryGcData(agentStat.getMemoryGc());
            addCpuLoadData(agentStat.getCpuLoad());
        }
    }

    public void buildCharts() {
        for (ChartType chartType : ChartType.values()) {
            this.charts.put(chartType, this.chartBuilders.get(chartType).buildChart());
        }
    }

    private void addMemoryGcData(AgentStatMemoryGcBo data) {
        if (data == null) {
            return;
        }
        this.type = data.getGcType();
        long timestamp = data.getTimestamp();
        ((SampledTimeSeriesLongChartBuilder)this.chartBuilders.get(ChartType.JVM_MEMORY_HEAP_USED)).addDataPoint(new DataPoint<Long, Long>(timestamp, data.getJvmMemoryHeapUsed()));
        ((SampledTimeSeriesLongChartBuilder)this.chartBuilders.get(ChartType.JVM_MEMORY_HEAP_MAX)).addDataPoint(new DataPoint<Long, Long>(timestamp, data.getJvmMemoryHeapMax()));
        ((SampledTimeSeriesLongChartBuilder)this.chartBuilders.get(ChartType.JVM_MEMORY_NON_HEAP_USED)).addDataPoint(new DataPoint<Long, Long>(timestamp, data.getJvmMemoryNonHeapUsed()));
        ((SampledTimeSeriesLongChartBuilder)this.chartBuilders.get(ChartType.JVM_MEMORY_NON_HEAP_MAX)).addDataPoint(new DataPoint<Long, Long>(timestamp, data.getJvmMemoryNonHeapMax()));
        ((SampledTimeSeriesLongChartBuilder)this.chartBuilders.get(ChartType.JVM_GC_OLD_COUNT)).addDataPoint(new DataPoint<Long, Long>(timestamp, data.getJvmGcOldCount()));
        ((SampledTimeSeriesLongChartBuilder)this.chartBuilders.get(ChartType.JVM_GC_OLD_TIME)).addDataPoint(new DataPoint<Long, Long>(timestamp, data.getJvmGcOldTime()));
    }

    private void addCpuLoadData(AgentStatCpuLoadBo data) {
        if (data == null) {
            return;
        }
        long timestamp = data.getTimestamp();
        double jvmCpuLoadPercentage = data.getJvmCpuLoad() * 100;
        double systemCpuLoadPercentage = data.getSystemCpuLoad() * 100;
        ((SampledTimeSeriesDoubleChartBuilder)this.chartBuilders.get(ChartType.CPU_LOAD_JVM)).addDataPoint(new DataPoint<Long, Double>(timestamp, jvmCpuLoadPercentage));
        ((SampledTimeSeriesDoubleChartBuilder)this.chartBuilders.get(ChartType.CPU_LOAD_SYSTEM)).addDataPoint(new DataPoint<Long, Double>(timestamp, systemCpuLoadPercentage));
    }

    public String getType() {
        return type;
    }

    public Map<ChartType, Chart> getCharts() {
        return charts;
    }

}