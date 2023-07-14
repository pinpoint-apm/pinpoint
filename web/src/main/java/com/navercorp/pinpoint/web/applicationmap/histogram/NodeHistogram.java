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
import com.navercorp.pinpoint.web.view.AgentResponseTimeViewModelList;
import com.navercorp.pinpoint.web.view.TimeViewModel;
import com.navercorp.pinpoint.web.view.histogram.AgentHistogramView;
import com.navercorp.pinpoint.web.view.histogram.HistogramView;
import com.navercorp.pinpoint.web.view.histogram.TimeHistogramChartBuilder;
import com.navercorp.pinpoint.web.view.histogram.TimeHistogramType;
import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesView;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.util.ArrayList;
import java.util.Collection;
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
    private Histogram applicationHistogram;

    // key is agentId
    private Map<String, Histogram> agentHistogramMap;

    private ApplicationTimeHistogram applicationTimeHistogram;

    private AgentTimeHistogram agentTimeHistogram;


    public NodeHistogram(Application application, Range range) {
        this.application = Objects.requireNonNull(application, "application");
        this.range = Objects.requireNonNull(range, "range");

        this.applicationHistogram = new Histogram(this.application.getServiceType());
        this.agentHistogramMap = new HashMap<>();

        this.applicationTimeHistogram = new ApplicationTimeHistogram(this.application, this.range);
        this.agentTimeHistogram = new AgentTimeHistogram(this.application, this.range);
    }

    public NodeHistogram(Application application, Range range, List<ResponseTime> responseHistogramList) {
        this.application = Objects.requireNonNull(application, "application");
        this.range = Objects.requireNonNull(range, "range");

        Objects.requireNonNull(responseHistogramList, "responseHistogramList");

        this.agentTimeHistogram = createAgentLevelTimeSeriesResponseTime(responseHistogramList);
        this.applicationTimeHistogram = createApplicationLevelTimeSeriesResponseTime(responseHistogramList);

        this.agentHistogramMap = createAgentLevelResponseTime(responseHistogramList);
        this.applicationHistogram = createApplicationLevelResponseTime(responseHistogramList);

    }


    public Histogram getApplicationHistogram() {
        return applicationHistogram;
    }

    public void setApplicationTimeHistogram(ApplicationTimeHistogram applicationTimeHistogram) {
        this.applicationTimeHistogram = applicationTimeHistogram;
    }

    public void setApplicationHistogram(Histogram applicationHistogram) {
        this.applicationHistogram = Objects.requireNonNull(applicationHistogram, "applicationHistogram");
    }

    public void setAgentHistogramMap(Map<String, Histogram> agentHistogramMap) {
        this.agentHistogramMap = agentHistogramMap;
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

    public List<TimeViewModel> getApplicationTimeHistogram(TimeHistogramFormat timeHistogramFormat) {
        return applicationTimeHistogram.createViewModel(timeHistogramFormat);
    }

    public TimeSeriesView getApplicationTimeHistogram(TimeHistogramType timeHistogramType) {
        TimeHistogramChartBuilder builder = new TimeHistogramChartBuilder(applicationTimeHistogram.getHistogramList());
        return builder.build(timeHistogramType);
    }

    public List<TimeHistogram> getApplicationTimeHistogramList() {
        return applicationTimeHistogram.getHistogramList();
    }

    public AgentResponseTimeViewModelList getAgentTimeHistogram(TimeHistogramFormat timeHistogramFormat) {
        return new AgentResponseTimeViewModelList(agentTimeHistogram.createViewModel(timeHistogramFormat));
    }

    public List<AgentHistogramView> getAgentHistogramViewList() {
        Map<String, List<TimeHistogram>> agentTimeHistogramMap = agentTimeHistogram.getAgentTimeHistogramMap();

        List<AgentHistogramView> agentHistogramViewList = new ArrayList<>();
        for (String agentId : agentHistogramMap.keySet()) {
            HistogramView histogramView = new HistogramView(agentHistogramMap.get(agentId), agentTimeHistogramMap.get(agentId));
            agentHistogramViewList.add(new AgentHistogramView(agentId, histogramView));
        }
        return agentHistogramViewList;
    }

    public void setAgentTimeHistogram(AgentTimeHistogram agentTimeHistogram) {
        this.agentTimeHistogram = agentTimeHistogram;
    }

    private ApplicationTimeHistogram createApplicationLevelTimeSeriesResponseTime(List<ResponseTime> responseHistogramList) {
        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(application, range);
        return builder.build(responseHistogramList);
    }


    private AgentTimeHistogram createAgentLevelTimeSeriesResponseTime(List<ResponseTime> responseHistogramList) {
        AgentTimeHistogramBuilder builder = new AgentTimeHistogramBuilder(application, range);
        AgentTimeHistogram histogram = builder.build(responseHistogramList);
        return histogram;
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
            agentHistogram = new Histogram(application.getServiceType());
            agentHistogramMap.put(agentId, agentHistogram);
        }
        agentHistogram.add(histogram);
    }

    private Histogram createApplicationLevelResponseTime(List<ResponseTime> responseHistogram) {
        final Histogram applicationHistogram = new Histogram(this.application.getServiceType());
        for (ResponseTime responseTime : responseHistogram) {
            final Collection<TimeHistogram> histogramList = responseTime.getAgentResponseHistogramList();
            for (Histogram histogram : histogramList) {
                applicationHistogram.add(histogram);
            }
        }
        return applicationHistogram;
    }

    public Range getRange() {
        return range;
    }
}
