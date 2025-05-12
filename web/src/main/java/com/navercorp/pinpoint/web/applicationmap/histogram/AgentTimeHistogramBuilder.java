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

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowDownSampler;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 */
public class AgentTimeHistogramBuilder {

    private final Application application;
    private final TimeWindow window;

    public AgentTimeHistogramBuilder(Application application, Range range) {
        this.application = Objects.requireNonNull(application, "application");
        this.window = new TimeWindow(range, TimeWindowDownSampler.SAMPLER);
    }

    public AgentTimeHistogramBuilder(Application application, TimeWindow window) {
        this.application = Objects.requireNonNull(application, "application");
        this.window = Objects.requireNonNull(window, "window");
    }

    public AgentTimeHistogram build(List<ResponseTime> responseHistogramList) {
        AgentHistogramList agentHistogramList = AgentHistogramList.newBuilder().build(application, responseHistogramList);
        return build(agentHistogramList);
    }

    public AgentTimeHistogram buildSource(LinkCallDataMap linkCallDataMap) {
        Objects.requireNonNull(linkCallDataMap, "linkCallDataMap");

        return build(linkCallDataMap.getInLinkList());
    }

    public AgentTimeHistogram buildTarget(LinkCallDataMap linkCallDataMap) {
        Objects.requireNonNull(linkCallDataMap, "linkCallDataMap");

        return build(linkCallDataMap.getOutLinkList());
    }


    public AgentTimeHistogram build(AgentHistogramList agentHistogramList) {
        List<AgentHistogram> agentHistograms = agentHistogramList.getAgentHistogramList();
        AgentHistogramList histogramList = interpolation(agentHistograms, window);
        return new AgentTimeHistogram(application, histogramList);
    }


    private AgentHistogramList interpolation(List<AgentHistogram> agentHistograms, TimeWindow window) {
        if (agentHistograms.isEmpty()) {
            return new AgentHistogramList();
        }

        // create window space. Prior to using a AgentHistogramList, we used a raw data structure.
        // could've been a list, but since range overflow may occur when applying filters, we use a map instead.
        // TODO: find better structure
        final AgentHistogramList.Builder resultAgentHistogramBuilder = AgentHistogramList.newBuilder();
        for (AgentHistogram agentHistogram : agentHistograms) {
            List<TimeHistogram> timeHistogramList = new ArrayList<>();
            for (Long time : window) {
                timeHistogramList.add(new TimeHistogram(application.getServiceType(), time));
            }
            Application agentId = agentHistogram.getAgentId();
            resultAgentHistogramBuilder.addTimeHistogram(agentId, timeHistogramList);
        }

        for (AgentHistogram agentHistogram : agentHistograms) {
            for (TimeHistogram timeHistogram : agentHistogram.getTimeHistogram()) {
                final long time = window.refineTimestamp(timeHistogram.getTimeStamp());
                Application agentId = agentHistogram.getAgentId();

                TimeHistogram newHistogram = new TimeHistogram(timeHistogram.getHistogramSchema(), time);
                newHistogram.add(timeHistogram);

                resultAgentHistogramBuilder.addTimeHistogram(agentId, newHistogram);
            }
        }

        return resultAgentHistogramBuilder.build();
    }


}
