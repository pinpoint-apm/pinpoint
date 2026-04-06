/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.view;

import com.navercorp.pinpoint.common.server.util.json.JsonFields;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.navercorp.pinpoint.web.view.id.AgentNameView;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.List;

public interface TimeHistogramView {

    List<TimeHistogramViewModel> build(Application application, List<TimeHistogram> histogramList);

    default JsonFields<AgentNameView, List<TimeHistogramViewModel>> build(AgentTimeHistogram histogram) {

        JsonFields.Builder<AgentNameView, List<TimeHistogramViewModel>> builder = JsonFields.newBuilder();

        for (AgentHistogram agentHistogram : histogram.getAgentHistogramList()) {
            List<TimeHistogram> timeHistogram = agentHistogram.getTimeHistogram();
            List<TimeHistogramViewModel> responseTimeViewModel = this.build(histogram.getApplication(), timeHistogram);
            builder.addField(new AgentNameView(agentHistogram.getAgentId().getApplicationName()), responseTimeViewModel);
        }
        return builder.build();
    }

    default List<TimeHistogramViewModel> build(ApplicationTimeHistogram histogram) {
        return build(histogram.getApplication(), histogram.getHistogramList());
    }

    // V1: key is slot("1s", "3s", "5s", "Slow", "Error"), value is {timestamp : count}
    TimeHistogramView ResponseTime = new ResponseTimeTimeHistogramBuilder();
    // V3: key is slot("1s", "3s", "5s", "Slow", "Error"), value is {count}, timestamp is in root
    TimeHistogramView TimeseriesHistogram = new TimeseriesHistogramBuilder();


    class ResponseTimeTimeHistogramBuilder implements TimeHistogramView {
        public List<TimeHistogramViewModel> build(Application application, List<TimeHistogram> histogramList) {
            return new ResponseTimeViewModelBuilder(application, histogramList).build();
        }
    }

    class TimeseriesHistogramBuilder implements TimeHistogramView {
        public List<TimeHistogramViewModel> build(Application application, List<TimeHistogram> histogramList) {
            return new TimeseriesHistogramViewBuilder(application, histogramList).build();
        }
    }
}
