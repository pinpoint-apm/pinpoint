/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.web.vo.stat.chart;

import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.SlotType;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.AgentStat;
import com.navercorp.pinpoint.web.vo.chart.LegacySampledTimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.DataPoint;
import com.navercorp.pinpoint.web.vo.chart.LegacySampledTitledTimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.chart.TitledDataPoint;
import com.navercorp.pinpoint.web.vo.stat.SampledActiveTrace;
import com.navercorp.pinpoint.web.vo.stat.SampledCpuLoad;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGc;
import com.navercorp.pinpoint.web.vo.stat.SampledTransaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author harebox
 * @author HyunGil Jeong
 */
@Deprecated
public class LegacyAgentStatChartGroup implements AgentStatChartGroup {

    public enum AgentStatChartType implements ChartType {
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
        TPS_TOTAL,
        ACTIVE_TRACE_FAST,
        ACTIVE_TRACE_NORMAL,
        ACTIVE_TRACE_SLOW,
        ACTIVE_TRACE_VERY_SLOW
    }

    private static final int UNCOLLECTED_DATA = AgentStat.NOT_COLLECTED;
    private static final DownSampler<Integer> INTEGER_DOWN_SAMPLER = DownSamplers.getIntegerDownSampler(UNCOLLECTED_DATA);
    private static final DownSampler<Long> LONG_DOWN_SAMPLER = DownSamplers.getLongDownSampler(UNCOLLECTED_DATA);
    private static final DownSampler<Double> DOUBLE_DOWN_SAMPLER = DownSamplers.getDoubleDownSampler(UNCOLLECTED_DATA, 1);

    private String type;

    private LegacySampledTimeSeriesChartBuilder<DataPoint<Long, Long>, Long> heapUsedChartBuilder;
    private LegacySampledTimeSeriesChartBuilder<DataPoint<Long, Long>, Long> heapMaxChartBuilder;
    private LegacySampledTimeSeriesChartBuilder<DataPoint<Long, Long>, Long> nonHeapUsedChartBuilder;
    private LegacySampledTimeSeriesChartBuilder<DataPoint<Long, Long>, Long> nonHeapMaxChartBuilder;
    private LegacySampledTimeSeriesChartBuilder<DataPoint<Long, Long>, Long> gcOldCountChartBuilder;
    private LegacySampledTimeSeriesChartBuilder<DataPoint<Long, Long>, Long> gcOldTimeChartBuilder;

    private LegacySampledTimeSeriesChartBuilder<DataPoint<Long, Double>, Double> jvmCpuLoadChartBuilder;
    private LegacySampledTimeSeriesChartBuilder<DataPoint<Long, Double>, Double> systemCpuLoadChartBuilder;

    private LegacySampledTimeSeriesChartBuilder<DataPoint<Long, Double>, Double> tpsSampledNewChartBuilder;
    private LegacySampledTimeSeriesChartBuilder<DataPoint<Long, Double>, Double> tpsSampledContinuationChartBuilder;
    private LegacySampledTimeSeriesChartBuilder<DataPoint<Long, Double>, Double> tpsUnsampledNewChartBuilder;
    private LegacySampledTimeSeriesChartBuilder<DataPoint<Long, Double>, Double> tpsUnsampledContinuationChartBuilder;
    private LegacySampledTimeSeriesChartBuilder<DataPoint<Long, Double>, Double> tpsTotalChartBuilder;

    private LegacySampledTitledTimeSeriesChartBuilder<Integer> activeTraceFastChartBuilder;
    private LegacySampledTitledTimeSeriesChartBuilder<Integer> activeTraceNormalChartBuilder;
    private LegacySampledTitledTimeSeriesChartBuilder<Integer> activeTraceSlowChartBuilder;
    private LegacySampledTitledTimeSeriesChartBuilder<Integer> activeTraceVerySlowChartBuilder;

    private final Map<ChartType, Chart> charts;

    private LegacyAgentStatChartGroup(Map<ChartType, Chart> charts, String gcType) {
        this.charts = charts;
        this.type = gcType;
    }

    public LegacyAgentStatChartGroup(TimeWindow timeWindow) {
        this.charts = new HashMap<>();

        this.heapUsedChartBuilder = new LegacySampledTimeSeriesChartBuilder<>(LONG_DOWN_SAMPLER, 0, timeWindow);
        this.heapMaxChartBuilder = new LegacySampledTimeSeriesChartBuilder<>(LONG_DOWN_SAMPLER, 0, timeWindow);
        this.nonHeapUsedChartBuilder = new LegacySampledTimeSeriesChartBuilder<>(LONG_DOWN_SAMPLER, 0, timeWindow);
        this.nonHeapMaxChartBuilder = new LegacySampledTimeSeriesChartBuilder<>(LONG_DOWN_SAMPLER, 0, timeWindow);
        this.gcOldCountChartBuilder = new LegacySampledTimeSeriesChartBuilder<>(LONG_DOWN_SAMPLER, 0, timeWindow);
        this.gcOldTimeChartBuilder = new LegacySampledTimeSeriesChartBuilder<>(LONG_DOWN_SAMPLER, 0, timeWindow);

        this.jvmCpuLoadChartBuilder = new LegacySampledTimeSeriesChartBuilder<>(DOUBLE_DOWN_SAMPLER, 1, timeWindow);
        this.systemCpuLoadChartBuilder = new LegacySampledTimeSeriesChartBuilder<>(DOUBLE_DOWN_SAMPLER, 1, timeWindow);

        this.tpsSampledNewChartBuilder = new LegacySampledTimeSeriesChartBuilder<>(DOUBLE_DOWN_SAMPLER, 1, timeWindow);
        this.tpsSampledContinuationChartBuilder = new LegacySampledTimeSeriesChartBuilder<>(DOUBLE_DOWN_SAMPLER, 1, timeWindow);
        this.tpsUnsampledNewChartBuilder = new LegacySampledTimeSeriesChartBuilder<>(DOUBLE_DOWN_SAMPLER, 1, timeWindow);
        this.tpsUnsampledContinuationChartBuilder = new LegacySampledTimeSeriesChartBuilder<>(DOUBLE_DOWN_SAMPLER, 1, timeWindow);
        this.tpsTotalChartBuilder = new LegacySampledTimeSeriesChartBuilder<>(DOUBLE_DOWN_SAMPLER, 1, timeWindow);

        this.activeTraceFastChartBuilder = new LegacySampledTitledTimeSeriesChartBuilder<>(INTEGER_DOWN_SAMPLER, 1, timeWindow);
        this.activeTraceNormalChartBuilder = new LegacySampledTitledTimeSeriesChartBuilder<>(INTEGER_DOWN_SAMPLER, 1, timeWindow);
        this.activeTraceSlowChartBuilder = new LegacySampledTitledTimeSeriesChartBuilder<>(INTEGER_DOWN_SAMPLER, 1, timeWindow);
        this.activeTraceVerySlowChartBuilder = new LegacySampledTitledTimeSeriesChartBuilder<>(INTEGER_DOWN_SAMPLER, 1, timeWindow);
    }

    public void addAgentStats(List<AgentStat> agentStats) {
        for (int i = agentStats.size() - 1; i >= 0; --i) {
            AgentStat agentStat = agentStats.get(i);
            if (agentStat != null) {
                AgentStat previous = i == agentStats.size() - 1 ? null : agentStats.get(i + 1);
                addMemoryGcData(agentStat, previous);
                addCpuLoadData(agentStat);
                addTransactionData(agentStat);
                addActiveTraceData(agentStat);
            }
        }
    }

    public void buildCharts() {
        if (this.charts.isEmpty()) {
            this.charts.put(AgentStatChartType.JVM_MEMORY_HEAP_USED, this.heapUsedChartBuilder.buildChart());
            this.charts.put(AgentStatChartType.JVM_MEMORY_HEAP_MAX, this.heapMaxChartBuilder.buildChart());
            this.charts.put(AgentStatChartType.JVM_MEMORY_NON_HEAP_USED, this.nonHeapUsedChartBuilder.buildChart());
            this.charts.put(AgentStatChartType.JVM_MEMORY_NON_HEAP_MAX, this.nonHeapMaxChartBuilder.buildChart());
            this.charts.put(AgentStatChartType.JVM_GC_OLD_COUNT, this.gcOldCountChartBuilder.buildChart());
            this.charts.put(AgentStatChartType.JVM_GC_OLD_TIME, this.gcOldTimeChartBuilder.buildChart());

            this.charts.put(AgentStatChartType.CPU_LOAD_JVM, this.jvmCpuLoadChartBuilder.buildChart());
            this.charts.put(AgentStatChartType.CPU_LOAD_SYSTEM, this.systemCpuLoadChartBuilder.buildChart());

            this.charts.put(AgentStatChartType.TPS_SAMPLED_NEW, this.tpsSampledNewChartBuilder.buildChart());
            this.charts.put(AgentStatChartType.TPS_SAMPLED_CONTINUATION, this.tpsSampledContinuationChartBuilder.buildChart());
            this.charts.put(AgentStatChartType.TPS_UNSAMPLED_NEW, this.tpsUnsampledNewChartBuilder.buildChart());
            this.charts.put(AgentStatChartType.TPS_UNSAMPLED_CONTINUATION, this.tpsUnsampledContinuationChartBuilder.buildChart());
            this.charts.put(AgentStatChartType.TPS_TOTAL, this.tpsTotalChartBuilder.buildChart());

            this.charts.put(AgentStatChartType.ACTIVE_TRACE_FAST, this.activeTraceFastChartBuilder.buildChart());
            this.charts.put(AgentStatChartType.ACTIVE_TRACE_NORMAL, this.activeTraceNormalChartBuilder.buildChart());
            this.charts.put(AgentStatChartType.ACTIVE_TRACE_SLOW, this.activeTraceSlowChartBuilder.buildChart());
            this.charts.put(AgentStatChartType.ACTIVE_TRACE_VERY_SLOW, this.activeTraceVerySlowChartBuilder.buildChart());
        }
    }

    private void addMemoryGcData(AgentStat agentStat, AgentStat previous) {
        this.type = agentStat.getGcType();
        long timestamp = agentStat.getTimestamp();
        if (agentStat.getHeapUsed() != UNCOLLECTED_DATA) {
            this.heapUsedChartBuilder.addDataPoint(new DataPoint<>(timestamp, agentStat.getHeapUsed()));
        }
        if (agentStat.getHeapMax() != UNCOLLECTED_DATA) {
            this.heapMaxChartBuilder.addDataPoint(new DataPoint<>(timestamp, agentStat.getHeapMax()));
        }
        if (agentStat.getNonHeapUsed() != UNCOLLECTED_DATA) {
            this.nonHeapUsedChartBuilder.addDataPoint(new DataPoint<>(timestamp, agentStat.getNonHeapUsed()));
        }
        if (agentStat.getNonHeapMax() != UNCOLLECTED_DATA) {
            this.nonHeapMaxChartBuilder.addDataPoint(new DataPoint<>(timestamp, agentStat.getNonHeapMax()));
        }
        if (previous != null) {
            if (checkJvmRestart(previous, agentStat)) {
                if (isGcCollected(agentStat)) {
                    this.gcOldCountChartBuilder.addDataPoint(new DataPoint<>(timestamp, agentStat.getGcOldCount()));
                    this.gcOldTimeChartBuilder.addDataPoint(new DataPoint<>(timestamp, agentStat.getGcOldTime()));
                } else {
                    agentStat.setGcOldCount(0L);
                    agentStat.setGcOldTime(0L);
                }
            } else {
                if (isGcCollected(agentStat) && isGcCollected(previous)) {
                    long gcOldCount = agentStat.getGcOldCount() - previous.getGcOldCount();
                    long gcOldTime = agentStat.getGcOldTime() - previous.getGcOldTime();
                    this.gcOldCountChartBuilder.addDataPoint(new DataPoint<>(timestamp, gcOldCount));
                    this.gcOldTimeChartBuilder.addDataPoint(new DataPoint<>(timestamp, gcOldTime));
                } else {
                    if (!isGcCollected(agentStat)) {
                        agentStat.setGcOldCount(previous.getGcOldCount());
                        agentStat.setGcOldTime(previous.getGcOldTime());
                    }
                }
            }
        } else {
            if (isGcCollected(agentStat)) {
                this.gcOldCountChartBuilder.addDataPoint(new DataPoint<>(timestamp, 0L));
                this.gcOldTimeChartBuilder.addDataPoint(new DataPoint<>(timestamp, 0L));
            }
        }
    }

    private boolean isGcCollected(AgentStat agentStat) {
        return agentStat.getGcOldCount() != UNCOLLECTED_DATA && agentStat.getGcOldTime() != UNCOLLECTED_DATA;
    }

    private boolean checkJvmRestart(AgentStat previous, AgentStat current) {
        long countDelta = current.getGcOldCount() - previous.getGcOldCount();
        long timeDelta = current.getGcOldTime() - previous.getGcOldTime();
        return countDelta < 0 && timeDelta < 0;
    }

    private void addCpuLoadData(AgentStat agentStat) {
        long timestamp = agentStat.getTimestamp();
        if (agentStat.getJvmCpuUsage() != UNCOLLECTED_DATA) {
            double jvmCpuUsage = agentStat.getJvmCpuUsage();
            this.jvmCpuLoadChartBuilder.addDataPoint(new DataPoint<>(timestamp, jvmCpuUsage * 100));
        }
        if (agentStat.getSystemCpuUsage() != UNCOLLECTED_DATA) {
            double systemCpuUsage = agentStat.getSystemCpuUsage();
            this.systemCpuLoadChartBuilder.addDataPoint(new DataPoint<>(timestamp, systemCpuUsage * 100));
        }
    }

    private void addTransactionData(AgentStat agentStat) {
        long timestamp = agentStat.getTimestamp();
        long interval = agentStat.getCollectInterval();
        if (interval > 0) {
            boolean isTransactionCollected = false;
            long total = 0;
            if (agentStat.getSampledNewCount() != UNCOLLECTED_DATA) {
                isTransactionCollected = true;
                double sampledNewTps = calculateTps(agentStat.getSampledNewCount(), interval);
                this.tpsSampledNewChartBuilder.addDataPoint(new DataPoint<>(timestamp, sampledNewTps));
                total += agentStat.getSampledNewCount();
            }
            if (agentStat.getSampledContinuationCount() != UNCOLLECTED_DATA) {
                isTransactionCollected = true;
                double sampledContinuationTps = calculateTps(agentStat.getSampledContinuationCount(), interval);
                this.tpsSampledContinuationChartBuilder.addDataPoint(new DataPoint<>(timestamp, sampledContinuationTps));
                total += agentStat.getSampledContinuationCount();
            }
            if (agentStat.getUnsampledNewCount() != UNCOLLECTED_DATA) {
                isTransactionCollected = true;
                double unsampledNewTps = calculateTps(agentStat.getUnsampledNewCount(), interval);
                this.tpsUnsampledNewChartBuilder.addDataPoint(new DataPoint<>(timestamp, unsampledNewTps));
                total += agentStat.getUnsampledNewCount();
            }
            if (agentStat.getUnsampledContinuationCount() != UNCOLLECTED_DATA) {
                isTransactionCollected = true;
                double unsampledContinuationTps = calculateTps(agentStat.getUnsampledContinuationCount(), interval);
                this.tpsUnsampledContinuationChartBuilder.addDataPoint(new DataPoint<>(timestamp, unsampledContinuationTps));
                total += agentStat.getUnsampledContinuationCount();
            }
            if (isTransactionCollected) {
                double totalTps = calculateTps(total, interval);
                this.tpsTotalChartBuilder.addDataPoint(new DataPoint<>(timestamp, totalTps));
            }
        }
    }

    private void addActiveTraceData(AgentStat agentStat) {
        long timestamp = agentStat.getTimestamp();
        HistogramSchema schema = agentStat.getHistogramSchema();
        if (schema != null) {
            Map<SlotType, Integer> activeTraceCounts = agentStat.getActiveTraceCounts();
            int fastCount = activeTraceCounts.get(SlotType.FAST);
            if (fastCount != UNCOLLECTED_DATA) {
                TitledDataPoint<Long, Integer> fastDataPoint = new TitledDataPoint<>(schema.getFastSlot().getSlotName(), timestamp, fastCount);
                this.activeTraceFastChartBuilder.addDataPoint(fastDataPoint);
            }
            int normalCount = activeTraceCounts.get(SlotType.NORMAL);
            if (normalCount != UNCOLLECTED_DATA) {
                TitledDataPoint<Long, Integer> normalDataPoint = new TitledDataPoint<>(schema.getNormalSlot().getSlotName(), timestamp, normalCount);
                this.activeTraceNormalChartBuilder.addDataPoint(normalDataPoint);
            }
            int slowCount = activeTraceCounts.get(SlotType.SLOW);
            if (slowCount != UNCOLLECTED_DATA) {
                TitledDataPoint<Long, Integer> slowDataPoint = new TitledDataPoint<>(schema.getSlowSlot().getSlotName(), timestamp, slowCount);
                this.activeTraceSlowChartBuilder.addDataPoint(slowDataPoint);
            }
            int verySlowCount = activeTraceCounts.get(SlotType.VERY_SLOW);
            if (verySlowCount != UNCOLLECTED_DATA) {
                TitledDataPoint<Long, Integer> verySlowDataPoint = new TitledDataPoint<>(schema.getVerySlowSlot().getSlotName(), timestamp, verySlowCount);
                this.activeTraceVerySlowChartBuilder.addDataPoint(verySlowDataPoint);
            }
        }
    }

    private double calculateTps(long count, long intervalMs) {
        final int numDecimal = 1;
        return AgentStatUtils.calculateRate(count, intervalMs, numDecimal, UNCOLLECTED_DATA);
    }

    public String getType() {
        return type;
    }

    public Map<ChartType, Chart> getCharts() {
        return charts;
    }

    public static class LegacyAgentStatChartGroupBuilder {
        private final TimeWindow timeWindow;
        private List<SampledJvmGc> jvmGcs;
        private List<SampledCpuLoad> cpuLoads;
        private List<SampledTransaction> transactions;
        private List<SampledActiveTrace> activeTraces;

        public LegacyAgentStatChartGroupBuilder(TimeWindow timeWindow) {
            this.timeWindow = timeWindow;
        }

        public LegacyAgentStatChartGroupBuilder jvmGcs(List<SampledJvmGc> jvmGcs) {
            this.jvmGcs = jvmGcs;
            return this;
        }

        public LegacyAgentStatChartGroupBuilder cpuLoads(List<SampledCpuLoad> cpuLoads) {
            this.cpuLoads = cpuLoads;
            return this;
        }

        public LegacyAgentStatChartGroupBuilder transactions(List<SampledTransaction> transactions) {
            this.transactions = transactions;
            return this;
        }

        public LegacyAgentStatChartGroupBuilder activeTraces(List<SampledActiveTrace> activeTraces) {
            this.activeTraces = activeTraces;
            return this;
        }

        public LegacyAgentStatChartGroup build() {
            Map<ChartType, Chart> charts = new HashMap<>();
            JvmGcChartGroup jvmGcChartGroup = new JvmGcChartGroup(this.timeWindow, this.jvmGcs);
            String gcType = jvmGcChartGroup.getType();
            charts.putAll(jvmGcChartGroup.getCharts());
            charts.putAll(new CpuLoadChartGroup(this.timeWindow, this.cpuLoads).getCharts());
            charts.putAll(new TransactionChartGroup(this.timeWindow, this.transactions).getCharts());
            charts.putAll(new ActiveTraceChartGroup(this.timeWindow, this.activeTraces).getCharts());
            return new LegacyAgentStatChartGroup(charts, gcType);
        }
    }
}