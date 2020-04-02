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

package com.navercorp.pinpoint.web.applicationmap.rawdata;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class AgentHistogramList {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // stores times series data per agent
    private final Map<Application, AgentHistogram> agentHistogramMap = new HashMap<>();

    public AgentHistogramList() {
    }

    public AgentHistogramList(Application application, List<ResponseTime> responseHistogramList) {
        Objects.requireNonNull(responseHistogramList, "responseHistogramList");

        for (ResponseTime responseTime : responseHistogramList) {
            for (Map.Entry<String, TimeHistogram> agentEntry : responseTime.getAgentHistogram()) {
                TimeHistogram timeHistogram = agentEntry.getValue();
                this.addAgentHistogram(agentEntry.getKey(), application.getServiceType(), timeHistogram);
            }
        }
    }


    public void addTimeHistogram(Application agentId, Collection<TimeHistogram> histogramList) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(histogramList, "histogramList");

        AgentHistogram agentHistogram = getAgentHistogram(agentId);
        agentHistogram.addTimeHistogram(histogramList);
    }

    public void addTimeHistogram(Application agentId, TimeHistogram timeHistogram) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(timeHistogram, "timeHistogram");

        AgentHistogram agentHistogram = getAgentHistogram(agentId);
        agentHistogram.addTimeHistogram(timeHistogram);
    }

    public void addAgentHistogram(String agentName, ServiceType serviceType, Collection<TimeHistogram> histogramList) {
        Application agentId = new Application(agentName, serviceType);
        addTimeHistogram(agentId, histogramList);
    }

    public void addAgentHistogram(String agentName, ServiceType serviceType, TimeHistogram timeHistogram) {
        Application agentId = new Application(agentName, serviceType);
        addTimeHistogram(agentId, timeHistogram);
    }



    private AgentHistogram getAgentHistogram(Application agentId) {
        Objects.requireNonNull(agentId, "agentId");

        AgentHistogram agentHistogram = agentHistogramMap.computeIfAbsent(agentId, k -> new AgentHistogram(agentId));
        return agentHistogram;
    }

    public Histogram mergeHistogram(ServiceType serviceType) {
        final Histogram histogram = new Histogram(serviceType);
        for (AgentHistogram agentHistogram : getAgentHistogramList()) {
            histogram.add(agentHistogram.getHistogram());
        }
        return histogram;
    }



    public void addAgentHistogram(AgentHistogram agentHistogram) {
        if (agentHistogram == null) {
            throw new NullPointerException("agentHistogram");
        }
        final String hostName = agentHistogram.getId();
        ServiceType serviceType = agentHistogram.getServiceType();

        Application agentId = new Application(hostName, serviceType);
        AgentHistogram findAgentHistogram = getAgentHistogram(agentId);
        findAgentHistogram.addTimeHistogram(agentHistogram.getTimeHistogram());
    }

    public void addAgentHistogram(AgentHistogramList addAgentHistogramList) {
        if (addAgentHistogramList == null) {
            throw new NullPointerException("agentHistogram");
        }
        for (AgentHistogram agentHistogram : addAgentHistogramList.agentHistogramMap.values()) {
            addAgentHistogram(agentHistogram);
        }
    }

    public Collection<AgentHistogram> getAgentHistogramList() {
        return agentHistogramMap.values();
    }

    @Override
    public String toString() {
        return "AgentHistogramList{"
                    + agentHistogramMap +
                '}';
    }

    public int size() {
        return this.agentHistogramMap.size();
    }
}
