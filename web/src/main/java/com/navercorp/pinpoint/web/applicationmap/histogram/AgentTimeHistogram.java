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

import com.google.common.collect.Ordering;
import com.navercorp.pinpoint.common.server.util.json.JsonField;
import com.navercorp.pinpoint.common.server.util.json.JsonFields;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.view.TimeViewModel;
import com.navercorp.pinpoint.web.view.id.AgentNameView;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.stat.SampledApdexScore;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.application.DoubleApplicationStatPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * most of the features have been delegated to AgentHistogramList upon refactoring
 * TODO: functionality reduced to creating views - need to be renamed or removed
 *
 * @author emeroad
 */
public class AgentTimeHistogram {

    private static final Double DEFAULT_MIN_APDEX_SCORE = 2D;
    private static final Double DEFAULT_MAX_APDEX_SCORE = -2D;
    private static final String DEFAULT_AGENT_ID = "defaultAgentId";

    private static final Comparator<JsonField<AgentNameView, List<TimeViewModel>>> AGENT_NAME_COMPARATOR
            = Comparator.comparing((jsonField) -> jsonField.name().agentName());

    private static final Ordering<TimeHistogram> histogramOrdering = Ordering.from(TimeHistogram.TIME_STAMP_ASC_COMPARATOR);

    private final Application application;
    private final AgentHistogramList agentHistogramList;

    public AgentTimeHistogram(Application application) {
        this.application = Objects.requireNonNull(application, "application");
        this.agentHistogramList = new AgentHistogramList();
    }

    public AgentTimeHistogram(Application application, AgentHistogramList agentHistogramList) {
        this.application = Objects.requireNonNull(application, "application");
        this.agentHistogramList = Objects.requireNonNull(agentHistogramList, "agentHistogramList");
    }

    public JsonFields<AgentNameView, List<TimeViewModel>> createViewModel(TimeHistogramFormat timeHistogramFormat) {

        JsonFields.Builder<AgentNameView, List<TimeViewModel>> builder = JsonFields.newBuilder();
        builder.comparator(AGENT_NAME_COMPARATOR);
        for (AgentHistogram agentHistogram : agentHistogramList.getAgentHistogramList()) {
            Application agentId = agentHistogram.getAgentId();
            List<TimeHistogram> timeList = histogramOrdering.sortedCopy(agentHistogram.getTimeHistogram());
            JsonField<AgentNameView, List<TimeViewModel>> model = createAgentResponseTimeViewModel(agentId, timeList, timeHistogramFormat);
            builder.addField(model);
        }
        return builder.build();
    }

    public Map<String, List<TimeHistogram>> getTimeHistogramMap() {
        Map<String, List<TimeHistogram>> result = new HashMap<>();
        for (AgentHistogram agentHistogram : agentHistogramList.getAgentHistogramList()) {
            List<TimeHistogram> histogram = histogramOrdering.sortedCopy(agentHistogram.getTimeHistogram());
            result.put(agentHistogram.getAgentId().name(), histogram);
        }
        return result;
    }


    private JsonField<AgentNameView, List<TimeViewModel>> createAgentResponseTimeViewModel(Application agentName, List<TimeHistogram> timeHistogramList, TimeHistogramFormat timeHistogramFormat) {
        List<TimeViewModel> responseTimeViewModel = createResponseTimeViewModel(timeHistogramList, timeHistogramFormat);
        return JsonField.of(AgentNameView.of(agentName), responseTimeViewModel);
    }

    private List<TimeViewModel> createResponseTimeViewModel(List<TimeHistogram> timeHistogramList, TimeHistogramFormat timeHistogramFormat) {
        TimeViewModel.Builder format = TimeViewModel.newBuilder(timeHistogramFormat);
        return format.build(application, timeHistogramList);
    }

    public List<SampledApdexScore> getSampledAgentApdexScoreList(String agentName) {
        AgentHistogram agentHistogram = selectAgentHistogram(agentName);
        if (agentHistogram == null) {
            return Collections.emptyList();
        }

        List<SampledApdexScore> result = new ArrayList<>();
        for (TimeHistogram timeHistogram : agentHistogram.getTimeHistogram()) {
            if (timeHistogram.getTotalCount() != 0) {
                AgentStatPoint<Double> agentStatPoint = new AgentStatPoint<>(timeHistogram.getTimeStamp(), ApdexScore.toDoubleFromHistogram(timeHistogram));
                result.add(new SampledApdexScore(agentStatPoint));
            }
        }
        return result;
    }

    private AgentHistogram selectAgentHistogram(String agentName) {
        for (AgentHistogram agentHistogram : agentHistogramList.getAgentHistogramList()) {
            Application agentId = agentHistogram.getAgentId();
            if (agentId.name().equals(agentName)) {
                return agentHistogram;
            }
        }
        return null;
    }

    public List<DoubleApplicationStatPoint> getApplicationApdexScoreList(TimeWindow window) {
        int size = (int) window.getWindowRangeCount();
        List<Double> min = fillList(size, DEFAULT_MIN_APDEX_SCORE);
        List<String> minAgentId = fillList(size, DEFAULT_AGENT_ID);
        List<Double> max = fillList(size, DEFAULT_MAX_APDEX_SCORE);
        List<String> maxAgentId = fillList(size, DEFAULT_AGENT_ID);

        List<Histogram> sumHistogram = getDefaultHistograms(window, application.serviceType());

        for (AgentHistogram agentHistogram : agentHistogramList.getAgentHistogramList()) {
            for (TimeHistogram timeHistogram : agentHistogram.getTimeHistogram()) {
                if (timeHistogram.getTotalCount() != 0) {
                    int index = window.getWindowIndex(timeHistogram.getTimeStamp());
                    if (index < 0 || index >= size) {
                        continue;
                    }
                    double apdex = ApdexScore.toDoubleFromHistogram(timeHistogram);
                    String agentId = agentHistogram.getId();

                    updateMinMaxValue(index, apdex, agentId, min, minAgentId, max, maxAgentId);
                    sumHistogram.get(index).add(timeHistogram);
                }
            }
        }

        return createDoubleApplicationStatPoints(window, min, minAgentId, max, maxAgentId, sumHistogram);
    }

    private <T> List<T> fillList(int size, T defaultValue) {
        return new ArrayList<>(Collections.nCopies(size, defaultValue));
    }

    private void updateMinMaxValue(int index, double apdex, String agentId,
                                   List<Double> min, List<String> minAgentId, List<Double> max, List<String> maxAgentId) {
        if (min.get(index) > apdex) {
            min.set(index, apdex);
            minAgentId.set(index, agentId);
        }
        if (max.get(index) < apdex) {
            max.set(index, apdex);
            maxAgentId.set(index, agentId);
        }
    }

    private List<DoubleApplicationStatPoint> createDoubleApplicationStatPoints(TimeWindow window, List<Double> min, List<String> minAgentId, List<Double> max, List<String> maxAgentId, List<Histogram> sumHistogram) {
        List<DoubleApplicationStatPoint> applicationStatPoints = new ArrayList<>();
        for (long timestamp : window) {
            int index = window.getWindowIndex(timestamp);
            Histogram histogram = sumHistogram.get(index);
            if (histogram.getTotalCount() != 0) {
                double avg = ApdexScore.toDoubleFromHistogram(histogram);
                DoubleApplicationStatPoint point = new DoubleApplicationStatPoint(timestamp, min.get(index), minAgentId.get(index), max.get(index), maxAgentId.get(index), avg);
                applicationStatPoints.add(point);
            }
        }
        return applicationStatPoints;
    }

    private List<Histogram> getDefaultHistograms(TimeWindow window, ServiceType serviceType) {
        List<Histogram> sum = new ArrayList<>((int) window.getWindowRangeCount());
        for (long timestamp : window) {
            sum.add(new TimeHistogram(serviceType, timestamp));
        }
        return sum;
    }
}
