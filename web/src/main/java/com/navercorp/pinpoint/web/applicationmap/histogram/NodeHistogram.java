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

package com.navercorp.pinpoint.web.applicationmap.histogram;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.view.histogram.HistogramView;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * this class is a collection of
 * applicationHistogram
 * agentHistogram
 * applicationTimeHistogram
 * agentTimeHistogram
 *
 * @author emeroad
 */
public class NodeHistogram {

    private final Application application;

    private final Range range;

    // ApplicationLevelHistogram
    private final Histogram applicationHistogram;

    // key is agentId
    private final Map<String, Histogram> agentHistogramMap;

    private final ApplicationTimeHistogram applicationTimeHistogram;

    private final AgentTimeHistogram agentTimeHistogram;


    NodeHistogram(Application application, Range range) {
        this.application = Objects.requireNonNull(application, "application");
        this.range = Objects.requireNonNull(range, "range");

        this.applicationHistogram = new Histogram(this.application.serviceType());
        this.agentHistogramMap = Collections.emptyMap();

        this.applicationTimeHistogram = new ApplicationTimeHistogram(this.application);
        this.agentTimeHistogram = new AgentTimeHistogram(this.application);
    }

    NodeHistogram(Builder builder) {
        Objects.requireNonNull(builder, "builder");

        this.application = builder.application;
        this.range = builder.range;

        if (builder.applicationHistogram == null) {
            this.applicationHistogram = new Histogram(this.application.serviceType());
        } else {
            this.applicationHistogram = builder.applicationHistogram;
        }

        this.agentHistogramMap = Objects.requireNonNullElseGet(builder.agentHistogramMap, Collections::emptyMap);

        if (builder.applicationTimeHistogram == null) {
            this.applicationTimeHistogram = new ApplicationTimeHistogram(this.application);
        } else {
            this.applicationTimeHistogram = builder.applicationTimeHistogram;
        }
        if (builder.agentTimeHistogram == null) {
            this.agentTimeHistogram = new AgentTimeHistogram(this.application);
        } else {
            this.agentTimeHistogram = builder.agentTimeHistogram;
        }
    }

    public Histogram getApplicationHistogram() {
        return applicationHistogram;
    }

    public ApplicationTimeHistogram getApplicationTimeHistogram() {
        return applicationTimeHistogram;
    }

    public Map<String, Histogram> getAgentHistogramMap() {
        return agentHistogramMap;
    }

    public Map<String, ResponseTimeStatics> getAgentResponseStatisticsMap() {
        if (agentHistogramMap == null) {
            return null;
        }
        Map<String, ResponseTimeStatics> map = new HashMap<>(agentHistogramMap.size());
        agentHistogramMap.forEach((agentId, histogram) ->
                map.put(agentId, ResponseTimeStatics.fromHistogram(histogram))
        );
        return map;
    }


    public AgentTimeHistogram getAgentTimeHistogram() {
        return agentTimeHistogram;
    }

    public List<HistogramView> createAgentHistogramViewList() {
        Map<String, List<TimeHistogram>> agentTimeHistogramMap = agentTimeHistogram.getTimeHistogramMap();
        List<HistogramView> result = new ArrayList<>();
        for (Map.Entry<String, Histogram> entry : agentHistogramMap.entrySet()) {
            String agentId = entry.getKey();
            Histogram agentHistogram = entry.getValue();

            List<TimeHistogram> sortedTimeHistogram = agentTimeHistogramMap.computeIfAbsent(agentId, id -> Collections.emptyList());

            HistogramView histogramView = new HistogramView(agentId, agentHistogram, sortedTimeHistogram);
            result.add(histogramView);
        }
        return result;
    }

    public Range getRange() {
        return range;
    }

    public static Builder newBuilder(Application application, Range range) {
        return new Builder(application, range);
    }


    public static NodeHistogram empty(Application application, Range range) {
        return new NodeHistogram(application, range);
    }

    public static class Builder {

        final Application application;
        final Range range;

        // ApplicationLevelHistogram
        Histogram applicationHistogram;

        // key is agentId
        Map<String, Histogram> agentHistogramMap = Collections.emptyMap();

        ApplicationTimeHistogram applicationTimeHistogram;

        AgentTimeHistogram agentTimeHistogram;

        Builder(Application application, Range range) {
            this.application = Objects.requireNonNull(application, "application");
            this.range = Objects.requireNonNull(range, "range");
        }

        public void setApplicationTimeHistogram(ApplicationTimeHistogram applicationTimeHistogram) {
            this.applicationTimeHistogram = applicationTimeHistogram;
        }

        public void setApplicationHistogram(Histogram applicationHistogram) {
            this.applicationHistogram = Objects.requireNonNull(applicationHistogram, "applicationHistogram");
        }

        public void setAgentTimeHistogram(AgentTimeHistogram agentTimeHistogram) {
            this.agentTimeHistogram = agentTimeHistogram;
        }

        public void setAgentHistogramMap(Map<String, Histogram> agentHistogramMap) {
            this.agentHistogramMap = agentHistogramMap;
        }

        public void setApplicationHistogram(List<ResponseTime> responseTimeList) {
            Objects.requireNonNull(responseTimeList, "responseTimeList");
            this.applicationHistogram = createApplicationLevelResponseTime(responseTimeList);
        }

        public void setAgentHistogramMap(List<ResponseTime> responseTimeList) {
            Objects.requireNonNull(responseTimeList, "responseTimeList");
            this.agentHistogramMap = createAgentLevelResponseTime(responseTimeList);
        }

        public void setResponseHistogram(List<ResponseTime> responseHistogramList) {
            this.agentTimeHistogram = createAgentLevelTimeSeriesResponseTime(responseHistogramList);
            this.agentHistogramMap = createAgentLevelResponseTime(responseHistogramList);

            this.applicationTimeHistogram = createApplicationLevelTimeSeriesResponseTime(responseHistogramList);
            this.applicationHistogram = createApplicationLevelResponseTime(responseHistogramList);
        }

        private ApplicationTimeHistogram createApplicationLevelTimeSeriesResponseTime(List<ResponseTime> responseHistogramList) {
            ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(application, range);
            return builder.build(responseHistogramList);
        }


        private AgentTimeHistogram createAgentLevelTimeSeriesResponseTime(List<ResponseTime> responseHistogramList) {
            AgentTimeHistogramBuilder builder = new AgentTimeHistogramBuilder(application, range);
            return builder.build(responseHistogramList);
        }

        private Histogram createApplicationLevelResponseTime(List<ResponseTime> responseHistogram) {
            final Histogram applicationHistogram = new Histogram(this.application.serviceType());
            for (ResponseTime responseTime : responseHistogram) {
                final Collection<TimeHistogram> histogramList = responseTime.getAgentResponseHistogramList();
                applicationHistogram.addAll(histogramList);
            }
            return applicationHistogram;
        }

        private Map<String, Histogram> createAgentLevelResponseTime(List<ResponseTime> responseHistogramList) {
            Map<String, Histogram> agentHistogramMap = new HashMap<>();
            for (ResponseTime responseTime : responseHistogramList) {
                for (Map.Entry<String, TimeHistogram> entry : responseTime.getAgentHistogram()) {
                    addAgentLevelHistogram(agentHistogramMap, entry.getKey(), entry.getValue());
                }
            }
            return agentHistogramMap;
        }

        private void addAgentLevelHistogram(Map<String, Histogram> agentHistogramMap, String agentId, TimeHistogram histogram) {
            Histogram agentHistogram = agentHistogramMap.get(agentId);
            if (agentHistogram == null) {
                agentHistogram = new Histogram(application.serviceType());
                agentHistogramMap.put(agentId, agentHistogram);
            }
            agentHistogram.add(histogram);
        }

        public NodeHistogram build() {
            return new NodeHistogram(this);
        }
    }
}
