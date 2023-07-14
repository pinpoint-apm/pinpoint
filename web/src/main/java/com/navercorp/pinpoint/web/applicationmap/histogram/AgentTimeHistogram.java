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
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.view.AgentResponseTimeViewModel;
import com.navercorp.pinpoint.web.view.TimeViewModel;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.stat.SampledApdexScore;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.application.DoubleApplicationStatPoint;

import java.util.ArrayList;
import java.util.Collection;
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

    private static final Comparator<AgentResponseTimeViewModel> AGENT_NAME_COMPARATOR
            = Comparator.comparing(AgentResponseTimeViewModel::getAgentName);

    private final Application application;
    private final Range range;
    private final AgentHistogramList agentHistogramList;

    public AgentTimeHistogram(Application application, Range range) {
        this.application = Objects.requireNonNull(application, "application");
        this.range = Objects.requireNonNull(range, "range");
        this.agentHistogramList = new AgentHistogramList();
    }

    public AgentTimeHistogram(Application application, Range range, AgentHistogramList agentHistogramList) {
        this.application = Objects.requireNonNull(application, "application");
        this.range = Objects.requireNonNull(range, "range");
        this.agentHistogramList = Objects.requireNonNull(agentHistogramList, "agentHistogramList");
    }


    public List<AgentResponseTimeViewModel> createViewModel(TimeHistogramFormat timeHistogramFormat) {
        final List<AgentResponseTimeViewModel> result = new ArrayList<>();
        for (AgentHistogram agentHistogram : agentHistogramList.getAgentHistogramList()) {
            Application agentId = agentHistogram.getAgentId();
            List<TimeHistogram> timeList = sortTimeHistogram(agentHistogram.getTimeHistogram());
            AgentResponseTimeViewModel model = createAgentResponseTimeViewModel(agentId, timeList, timeHistogramFormat);
            result.add(model);
        }
        result.sort(AGENT_NAME_COMPARATOR);
        return result;
    }

    public Map<String, List<TimeHistogram>> getAgentTimeHistogramMap() {
        Map<String, List<TimeHistogram>> result = new HashMap<>();
        for (AgentHistogram agentHistogram : agentHistogramList.getAgentHistogramList()) {
            result.put(agentHistogram.getAgentId().getName(), sortTimeHistogram(agentHistogram.getTimeHistogram()));
        }
        return result;
    }

    private List<TimeHistogram> sortTimeHistogram(Collection<TimeHistogram> timeMap) {
        List<TimeHistogram> timeList = new ArrayList<>(timeMap);
        timeList.sort(TimeHistogram.TIME_STAMP_ASC_COMPARATOR);
        return timeList;
    }

    private AgentResponseTimeViewModel createAgentResponseTimeViewModel(Application agentName, List<TimeHistogram> timeHistogramList, TimeHistogramFormat timeHistogramFormat) {
        List<TimeViewModel> responseTimeViewModel = createResponseTimeViewModel(timeHistogramList, timeHistogramFormat);
        AgentResponseTimeViewModel agentResponseTimeViewModel = new AgentResponseTimeViewModel(agentName, responseTimeViewModel);
        return agentResponseTimeViewModel;
    }

    private List<TimeViewModel> createResponseTimeViewModel(List<TimeHistogram> timeHistogramList, TimeHistogramFormat timeHistogramFormat) {
        return new TimeViewModel.TimeViewModelBuilder(application, timeHistogramList).setTimeHistogramFormat(timeHistogramFormat).build();
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
            if (agentId.getName().equals(agentName)) {
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

        List<Histogram> sumHistogram = getDefaultHistograms(window, application.getServiceType());

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
