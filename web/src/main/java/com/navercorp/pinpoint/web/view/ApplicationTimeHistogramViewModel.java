/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
@JsonSerialize(using = ApplicationTimeHistogramViewModelSerializer.class)
public class ApplicationTimeHistogramViewModel {

    private final Application application;
    private final Range range;
    private final AgentHistogramList agentHistogramList;

    public ApplicationTimeHistogramViewModel(Application application, Range range, AgentHistogramList agentHistogramList) {
        this.application = application;
        this.range = range;
        this.agentHistogramList = agentHistogramList;
    }

    public List<AgentTimeHistogramSummary> getSummaryList() {
        List<AgentTimeHistogramSummary> agentTimeHistogramSummaryList = new ArrayList<>(agentHistogramList.size());

        for (AgentHistogram agentHistogram : agentHistogramList.getAgentHistogramList()) {
            agentTimeHistogramSummaryList.add(AgentTimeHistogramSummary.createSummary(agentHistogram));
        }

        return agentTimeHistogramSummaryList;
    }

    public List<AgentResponseTimeViewModel> getTimeSeriesViewModel() {
        AgentTimeHistogram histogram = new AgentTimeHistogram(application, range, agentHistogramList);
        List<AgentResponseTimeViewModel> timeSeriesViewModelList = histogram.createViewModel();
        return timeSeriesViewModelList;
    }

}
