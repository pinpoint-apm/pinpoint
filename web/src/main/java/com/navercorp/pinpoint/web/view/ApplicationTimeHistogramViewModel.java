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

import com.navercorp.pinpoint.common.server.util.json.JsonFields;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.applicationmap.view.TimeHistogramViewModel;
import com.navercorp.pinpoint.web.view.id.AgentNameView;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ApplicationTimeHistogramViewModel {

    private final TimeHistogramFormat format;
    private final Application application;
    private final AgentHistogramList agentHistogramList;


    public ApplicationTimeHistogramViewModel(TimeHistogramFormat format, Application application, AgentHistogramList agentHistogramList) {
        this.format = Objects.requireNonNull(format, "format");
        this.application = Objects.requireNonNull(application, "application");
        this.agentHistogramList = Objects.requireNonNull(agentHistogramList, "agentHistogramList");
    }

    /**
     * @return JsonFields(String:AgentId, Histogram)
     */
    public JsonFields<AgentNameView, Histogram> getSummary() {
        JsonFields.Builder<AgentNameView, Histogram> builder = JsonFields.newBuilder();
        for (AgentHistogram agentHistogram : agentHistogramList.getAgentHistogramList()) {
            AgentNameView agentName = AgentNameView.of(agentHistogram.getAgentId());
            Histogram histogram = agentHistogram.getHistogram();
            builder.addField(agentName, histogram);
        }
        return builder.build();
    }

    /**
     * @return JsonFields(String:AgentId, value:List<TimeViewModel>)
     */
    public JsonFields<AgentNameView, List<TimeHistogramViewModel>> getTimeSeries() {
        AgentTimeHistogram histogram = new AgentTimeHistogram(application, agentHistogramList);
        return histogram.createViewModel(format);
    }

}
