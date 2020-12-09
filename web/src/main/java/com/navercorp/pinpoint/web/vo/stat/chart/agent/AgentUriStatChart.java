/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.navercorp.pinpoint.common.trace.UriStatHistogramBucket;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.AgentStatPointFactory;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.SampledEachUriStatBo;
import com.navercorp.pinpoint.web.vo.stat.SampledUriStatHistogramBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Taejin Koo
 */
public class AgentUriStatChart implements StatChart {

    private static final String UNCOLLECTED_STRING = null;

    private static final AgentStatPointFactory DEFAULT_STAT_POINT_FACTORY = new AgentStatPointFactory(-1, -1L, -1D);

    private final String uri;

    private final AgentUriChartGroup agentUriChartGroup;
    private final AgentUriChartGroup failedAgentUriChartGroup;

    public AgentUriStatChart(TimeWindow timeWindow, List<SampledEachUriStatBo> sampledEachUriStatBoList) {
        SampledEachUriStatBo representative = ListUtils.getFirst(sampledEachUriStatBoList);
        if (representative == null) {
            this.uri = UNCOLLECTED_STRING;
        } else {
            this.uri = representative.getUri();
        }

        List<SampledUriStatHistogramBo> total = sampledEachUriStatBoList.stream().map(SampledEachUriStatBo::getTotalSampledUriStatHistogramBo).collect(Collectors.toList());
        this.agentUriChartGroup = new AgentUriChartGroup(timeWindow, total);

        List<SampledUriStatHistogramBo> failed = sampledEachUriStatBoList.stream().map(SampledEachUriStatBo::getFailedSampledUriStatHistogramBo).collect(Collectors.toList());
        this.failedAgentUriChartGroup = new AgentUriChartGroup(timeWindow, failed);
    }

    public String getUri() {
        return uri;
    }

    public long getTotalCount() {
        return agentUriChartGroup.totalCount;
    }

    public double getAvgTime() {
        return agentUriChartGroup.avgTime;
    }

    public long getMaxTime() {
        return agentUriChartGroup.maxTime;
    }

    @Override
    public StatChartGroup getCharts() {
        return agentUriChartGroup;
    }

//    public StatChartGroup getFailedCharts() {
//        return failedAgentUriChartGroup;
//    }

    public static class AgentUriChartGroup implements StatChartGroup {

        private final TimeWindow timeWindow;

        private final long totalCount;
        private final long maxTime;
        private final double avgTime;

        private final Map<ChartType, Chart<? extends Point>> charts;


        private AgentUriChartGroup(TimeWindow timeWindow, List<SampledUriStatHistogramBo> sampledUriStatHistogramBoList) {
            this.timeWindow = timeWindow;

            this.totalCount = sampledUriStatHistogramBoList.stream().mapToLong(o -> o.getCountPoint().getSumYVal()).sum();
            this.maxTime = sampledUriStatHistogramBoList.stream().mapToLong(o -> o.getMaxTimePoint().getMaxYVal()).max().getAsLong();
            if (totalCount != 0) {
                this.avgTime = sampledUriStatHistogramBoList.stream().mapToLong(o -> o.getTotalElapsedTime()).sum() / totalCount;
            } else {
                this.avgTime = 0L;
            }

            this.charts = newChart(sampledUriStatHistogramBoList);
        }

        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }


        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return charts;
        }

        public enum AgentUriChartType implements AgentChartType {
            COUNT,
            AVG,
            MAX;
        }

        public enum AgentUriHistogramType implements AgentChartType {
            HISTOGRAM_BUCKET;

            @Override
            public String[] getSchema() {
                UriStatHistogramBucket[] values = UriStatHistogramBucket.values();
                int length = values.length;
                String[] schemaDescs = new String[length];

                for (int i = 0; i < length; i++) {
                    schemaDescs[i] = values[i].getDesc();
                }

                return schemaDescs;
            }
        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<SampledUriStatHistogramBo> sampledEachUriStatHistogramBoList) {
            Map<ChartType, Chart<? extends Point>> totalCharts = new HashMap<>();

            Chart<AgentStatPoint<Integer>> totalCountChart = newIntChart(sampledEachUriStatHistogramBoList, SampledUriStatHistogramBo::getCountPoint);
            totalCharts.put(AgentUriChartType.COUNT, totalCountChart);

            Chart<AgentStatPoint<Double>> avgChart = newDoubleChart(sampledEachUriStatHistogramBoList, SampledUriStatHistogramBo::getAvgTimePoint);
            totalCharts.put(AgentUriChartType.AVG, avgChart);

            Chart<AgentStatPoint<Long>> maxElapsedChart = newLongChart(sampledEachUriStatHistogramBoList, SampledUriStatHistogramBo::getMaxTimePoint);
            totalCharts.put(AgentUriChartType.MAX, maxElapsedChart);

            List<UriStatHistogramPoint> histogramPoints = new ArrayList<>();
            for (SampledUriStatHistogramBo sampledUriStatHistogramBo : sampledEachUriStatHistogramBoList) {
                UriStatHistogramPoint uriStatHistogramPoint = createUriStatHistogramPoint(sampledUriStatHistogramBo);
                histogramPoints.add(uriStatHistogramPoint);
            }
            totalCharts.put(AgentUriHistogramType.HISTOGRAM_BUCKET, newHistogramPointChart(histogramPoints));

            return totalCharts;
        }

        private UriStatHistogramPoint createUriStatHistogramPoint(SampledUriStatHistogramBo sampledUriStatHistogramBo) {
            long xVal = sampledUriStatHistogramBo.getMaxTimePoint().getXVal();
            int[] uriStatHistogramValues = sampledUriStatHistogramBo.getUriStatHistogramValue();

            return new UriStatHistogramPoint(xVal, uriStatHistogramValues);
        }

        private Chart<AgentStatPoint<Integer>> newIntChart(List<SampledUriStatHistogramBo> sampledEachUriStatBoList, Function<SampledUriStatHistogramBo, AgentStatPoint<Integer>> function) {
            TimeSeriesChartBuilder<AgentStatPoint<Integer>> builder = new TimeSeriesChartBuilder<>(this.timeWindow, DEFAULT_STAT_POINT_FACTORY.getUncollectedIntValuePointCreator());
            return builder.build(sampledEachUriStatBoList, function);
        }

        private Chart<AgentStatPoint<Long>> newLongChart(List<SampledUriStatHistogramBo> sampledEachUriStatBoList, Function<SampledUriStatHistogramBo, AgentStatPoint<Long>> function) {
            TimeSeriesChartBuilder<AgentStatPoint<Long>> builder = new TimeSeriesChartBuilder<>(this.timeWindow, DEFAULT_STAT_POINT_FACTORY.getUncollectedLongValuePointCreator());
            return builder.build(sampledEachUriStatBoList, function);
        }

        private Chart<AgentStatPoint<Double>> newDoubleChart(List<SampledUriStatHistogramBo> sampledEachUriStatBoList, Function<SampledUriStatHistogramBo, AgentStatPoint<Double>> function) {
            TimeSeriesChartBuilder<AgentStatPoint<Double>> builder = new TimeSeriesChartBuilder<>(this.timeWindow, DEFAULT_STAT_POINT_FACTORY.getUncollectedDoubleValuePointCreator());
            return builder.build(sampledEachUriStatBoList, function);
        }

        private Chart<UriStatHistogramPoint> newHistogramPointChart(List<UriStatHistogramPoint> histogramPointList) {
            TimeSeriesChartBuilder<UriStatHistogramPoint> builder = new TimeSeriesChartBuilder<>(this.timeWindow, UNCOLLECTED_POINT_CREATOR);
            return builder.build(histogramPointList);
        }

    }

    public static final int[] UNCOLLECTED = UriStatHistogramBucket.createNewArrayValue();
    public static final Point.UncollectedPointCreator<UriStatHistogramPoint> UNCOLLECTED_POINT_CREATOR = new Point.UncollectedPointCreator<UriStatHistogramPoint>() {
        @Override
        public UriStatHistogramPoint createUnCollectedPoint(long xVal) {
            return new UriStatHistogramPoint(xVal, UNCOLLECTED);
        }
    };

}
