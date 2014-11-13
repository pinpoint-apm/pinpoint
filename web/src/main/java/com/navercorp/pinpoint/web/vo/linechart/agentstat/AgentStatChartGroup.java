package com.nhn.pinpoint.web.vo.linechart.agentstat;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nhn.pinpoint.common.bo.AgentStatCpuLoadBo;
import com.nhn.pinpoint.common.bo.AgentStatMemoryGcBo;
import com.nhn.pinpoint.web.vo.AgentStat;
import com.nhn.pinpoint.web.vo.linechart.DataPoint;
import com.nhn.pinpoint.web.vo.linechart.Chart;
import com.nhn.pinpoint.web.vo.linechart.Chart.LineChartBuilder;
import com.nhn.pinpoint.web.vo.linechart.SampledDataDoubleChartBuilder;
import com.nhn.pinpoint.web.vo.linechart.SampledDataLongChartBuilder;

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

    private String type;

    @JsonIgnore
    private final Map<ChartType, LineChartBuilder<? extends Number, ? extends Number>> chartBuilders;

    private final Map<ChartType, Chart> charts;

    public AgentStatChartGroup(int sampleRate) {
        this.chartBuilders = new EnumMap<ChartType, LineChartBuilder<? extends Number, ? extends Number>>(ChartType.class);
        this.chartBuilders.put(ChartType.JVM_MEMORY_HEAP_USED, new SampledDataLongChartBuilder(sampleRate));
        this.chartBuilders.put(ChartType.JVM_MEMORY_HEAP_MAX, new SampledDataLongChartBuilder(sampleRate));
        this.chartBuilders.put(ChartType.JVM_MEMORY_NON_HEAP_USED, new SampledDataLongChartBuilder(sampleRate));
        this.chartBuilders.put(ChartType.JVM_MEMORY_NON_HEAP_MAX, new SampledDataLongChartBuilder(sampleRate));
        this.chartBuilders.put(ChartType.JVM_GC_OLD_COUNT, new SampledDataLongChartBuilder(sampleRate));
        this.chartBuilders.put(ChartType.JVM_GC_OLD_TIME, new SampledDataLongChartBuilder(sampleRate));
        this.chartBuilders.put(ChartType.CPU_LOAD_JVM, new SampledDataDoubleChartBuilder(sampleRate));
        this.chartBuilders.put(ChartType.CPU_LOAD_SYSTEM, new SampledDataDoubleChartBuilder(sampleRate));
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
        ((SampledDataLongChartBuilder)this.chartBuilders.get(ChartType.JVM_MEMORY_HEAP_USED)).addDataPoint(new DataPoint<Long, Long>(timestamp, data.getJvmMemoryHeapUsed()));
        ((SampledDataLongChartBuilder)this.chartBuilders.get(ChartType.JVM_MEMORY_HEAP_MAX)).addDataPoint(new DataPoint<Long, Long>(timestamp, data.getJvmMemoryHeapMax()));
        ((SampledDataLongChartBuilder)this.chartBuilders.get(ChartType.JVM_MEMORY_NON_HEAP_USED)).addDataPoint(new DataPoint<Long, Long>(timestamp, data.getJvmMemoryNonHeapUsed()));
        ((SampledDataLongChartBuilder)this.chartBuilders.get(ChartType.JVM_MEMORY_NON_HEAP_MAX)).addDataPoint(new DataPoint<Long, Long>(timestamp, data.getJvmMemoryNonHeapMax()));
        ((SampledDataLongChartBuilder)this.chartBuilders.get(ChartType.JVM_GC_OLD_COUNT)).addDataPoint(new DataPoint<Long, Long>(timestamp, data.getJvmGcOldCount()));
        ((SampledDataLongChartBuilder)this.chartBuilders.get(ChartType.JVM_GC_OLD_TIME)).addDataPoint(new DataPoint<Long, Long>(timestamp, data.getJvmGcOldTime()));
    }

    private void addCpuLoadData(AgentStatCpuLoadBo data) {
        if (data == null) {
            return;
        }
        long timestamp = data.getTimestamp();
        double jvmCpuLoadPercentage = data.getJvmCpuLoad() * 100;
        double systemCpuLoadPercentage = data.getSystemCpuLoad() * 100;
        ((SampledDataDoubleChartBuilder)this.chartBuilders.get(ChartType.CPU_LOAD_JVM)).addDataPoint(new DataPoint<Long, Double>(timestamp, jvmCpuLoadPercentage));
        ((SampledDataDoubleChartBuilder)this.chartBuilders.get(ChartType.CPU_LOAD_SYSTEM)).addDataPoint(new DataPoint<Long, Double>(timestamp, systemCpuLoadPercentage));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<ChartType, Chart> getCharts() {
        return charts;
    }

}