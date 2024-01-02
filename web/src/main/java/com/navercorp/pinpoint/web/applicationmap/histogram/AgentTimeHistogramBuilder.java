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
import com.navercorp.pinpoint.common.trace.SlotType;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowDownSampler;
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
    private final Range range;
    private final TimeWindow window;

    public AgentTimeHistogramBuilder(Application application, Range range) {
        this.application = Objects.requireNonNull(application, "application");
        this.range = Objects.requireNonNull(range, "range");
        this.window = new TimeWindow(range, TimeWindowDownSampler.SAMPLER);
    }

    public AgentTimeHistogramBuilder(Application application, Range range, TimeWindow window) {
        this.application = Objects.requireNonNull(application, "application");
        this.range = Objects.requireNonNull(range, "range");
        this.window = Objects.requireNonNull(window, "window");
    }

    public AgentTimeHistogram build(List<ResponseTime> responseHistogramList) {
        AgentHistogramList agentHistogramList = new AgentHistogramList(application, responseHistogramList);
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


    private AgentTimeHistogram build(AgentHistogramList agentHistogramList) {
        AgentHistogramList histogramList = interpolation(agentHistogramList, window);
        return new AgentTimeHistogram(application, range, histogramList);
    }


    private AgentHistogramList interpolation(AgentHistogramList agentHistogramList, TimeWindow window) {
        if (agentHistogramList.size() == 0) {
            return new AgentHistogramList();
        }

        // create window space. Prior to using a AgentHistogramList, we used a raw data structure.
        // could've been a list, but since range overflow may occur when applying filters, we use a map instead.
        // TODO: find better structure
        final AgentHistogramList resultAgentHistogramList = new AgentHistogramList();
        for (AgentHistogram agentHistogram : agentHistogramList.getAgentHistogramList()) {
            List<TimeHistogram> timeHistogramList = new ArrayList<>();
            for (Long time : window) {
                timeHistogramList.add(new TimeHistogram(application.getServiceType(), time));
            }
            resultAgentHistogramList.addTimeHistogram(agentHistogram.getAgentId(), timeHistogramList);
        }

        for (AgentHistogram agentHistogram : agentHistogramList.getAgentHistogramList()) {
            for (TimeHistogram timeHistogram : agentHistogram.getTimeHistogram()) {
                final long time = window.refineTimestamp(timeHistogram.getTimeStamp());
                Application agentId = agentHistogram.getAgentId();
                TimeHistogram windowHistogram = new TimeHistogram(timeHistogram.getHistogramSchema(), time);
                windowHistogram.add(timeHistogram);
                resultAgentHistogramList.addTimeHistogram(agentId, windowHistogram);
            }
        }

        return resultAgentHistogramList;
    }


    public long getCount(TimeHistogram timeHistogram, SlotType slotType) {
        return timeHistogram.getCount(slotType);
    }


}
