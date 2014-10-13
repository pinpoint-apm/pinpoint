package com.nhn.pinpoint.web.vo.linechart.agentstat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import com.nhn.pinpoint.common.bo.AgentStatCpuLoadBo;
import com.nhn.pinpoint.common.bo.AgentStatMemoryGcBo;
import com.nhn.pinpoint.web.vo.AgentStat;
import com.nhn.pinpoint.web.vo.linechart.LineChart;
import com.nhn.pinpoint.web.vo.linechart.SampledDoubleLineChart;
import com.nhn.pinpoint.web.vo.linechart.SampledLongLineChart;

/**
 * @author harebox
 * @author hyungil.jeong
 */
public class AgentStatChartGroup {

    private static final String JVM_MEMORY_HEAP_USED_KEY = "jvmMemoryHeapUsed";
    private static final String JVM_MEMORY_HEAP_MAX_KEY = "jvmMemoryHeapMax";
    private static final String JVM_MEMORY_NON_HEAP_USED_KEY = "jvmMemoryNonHeapUsed";
    private static final String JVM_MEMORY_NON_HEAP_MAX_KEY = "jvmMemoryNonHeapMax";
    private static final String JVM_GC_OLD_COUNT_KEY = "jvmGcOldCount";
    private static final String JVM_GC_OLD_TIME_KEY = "jvmGcOldTime";

    private static final String CPU_LOAD_JVM_KEY = "jvmCpuLoad";
    private static final String CPU_LOAD_SYSTEM_KEY = "systemCpuLoad";

    private String type;
    private Map<String, LineChart<?, ?>> charts = new HashMap<String, LineChart<? extends Number, ? extends Number>>();

    public AgentStatChartGroup(int sampleRate) {
        charts.put(JVM_MEMORY_HEAP_USED_KEY, new SampledLongLineChart(sampleRate));
        charts.put(JVM_MEMORY_HEAP_MAX_KEY, new SampledLongLineChart(sampleRate));
        charts.put(JVM_MEMORY_NON_HEAP_USED_KEY, new SampledLongLineChart(sampleRate));
        charts.put(JVM_MEMORY_NON_HEAP_MAX_KEY, new SampledLongLineChart(sampleRate));
        charts.put(JVM_GC_OLD_COUNT_KEY, new SampledLongLineChart(sampleRate));
        charts.put(JVM_GC_OLD_TIME_KEY, new SampledLongLineChart(sampleRate));
        charts.put(CPU_LOAD_JVM_KEY, new SampledDoubleLineChart(sampleRate));
        charts.put(CPU_LOAD_SYSTEM_KEY, new SampledDoubleLineChart(sampleRate));
    }

    public void addAgentStats(List<AgentStat> agentStats) {
        for (AgentStat agentStat : agentStats) {
            addMemoryGcData(agentStat.getMemoryGc());
            addCpuLoadData(agentStat.getCpuLoad());
        }
        removeUncollectedCharts();
    }

    private void addMemoryGcData(AgentStatMemoryGcBo data) {
        if (data == null) {
            return;
        }
        this.type = data.getGcType();
        long timestamp = data.getTimestamp();
        ((SampledLongLineChart)charts.get(JVM_MEMORY_HEAP_USED_KEY)).addPoint(timestamp, data.getJvmMemoryHeapUsed());
        ((SampledLongLineChart)charts.get(JVM_MEMORY_HEAP_MAX_KEY)).addPoint(timestamp, data.getJvmMemoryHeapMax());
        ((SampledLongLineChart)charts.get(JVM_MEMORY_NON_HEAP_USED_KEY)).addPoint(timestamp, data.getJvmMemoryNonHeapUsed());
        ((SampledLongLineChart)charts.get(JVM_MEMORY_NON_HEAP_MAX_KEY)).addPoint(timestamp, data.getJvmMemoryNonHeapMax());
        ((SampledLongLineChart)charts.get(JVM_GC_OLD_COUNT_KEY)).addPoint(timestamp, data.getJvmGcOldCount());
        ((SampledLongLineChart)charts.get(JVM_GC_OLD_TIME_KEY)).addPoint(timestamp, data.getJvmGcOldTime());
    }

    private void addCpuLoadData(AgentStatCpuLoadBo data) {
        if (data == null) {
            return;
        }
        long timestamp = data.getTimestamp();
        double jvmCpuLoadPercentage = data.getJvmCpuLoad() * 100;
        double systemCpuLoadPercentage = data.getSystemCpuLoad() * 100;
        ((SampledDoubleLineChart)charts.get(CPU_LOAD_JVM_KEY)).addPoint(timestamp, jvmCpuLoadPercentage < 0 ? 0 : jvmCpuLoadPercentage);
        ((SampledDoubleLineChart)charts.get(CPU_LOAD_SYSTEM_KEY)).addPoint(timestamp, systemCpuLoadPercentage < 0 ? 0 : systemCpuLoadPercentage);
    }

    private void removeUncollectedCharts() {
        for (Iterator<Map.Entry<String, LineChart<?, ?>>> iter = charts.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<String, LineChart<?, ?>> chartEntry = iter.next();
            if (CollectionUtils.isEmpty(chartEntry.getValue().getPoints())) {
                iter.remove();
            }
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, LineChart<?, ?>> getCharts() {
        return charts;
    }

    public void setCharts(Map<String, LineChart<?, ?>> charts) {
        this.charts = charts;
    }

}