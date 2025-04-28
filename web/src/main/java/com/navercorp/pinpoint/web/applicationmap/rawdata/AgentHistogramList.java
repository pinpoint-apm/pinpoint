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

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author emeroad
 */
public class AgentHistogramList {

    // stores times series data per agent
    private final List<AgentHistogram> agentHistogramList;

    public AgentHistogramList() {
        this.agentHistogramList = List.of();
    }

    public AgentHistogramList(List<AgentHistogram> agentHistogramList) {
        this.agentHistogramList = Objects.requireNonNull(agentHistogramList, "agentHistogramList");
    }

    public Histogram mergeHistogram(HistogramSchema schema) {
        final Histogram histogram = new Histogram(schema);
        for (AgentHistogram agentHistogram : agentHistogramList) {
            histogram.add(agentHistogram.getHistogram());
        }
        return histogram;
    }

    public List<AgentHistogram> getAgentHistogramList() {
        return agentHistogramList;
    }

    @Override
    public String toString() {
        return "AgentHistogramList{"
                + agentHistogramList +
                '}';
    }

    public int size() {
        return this.agentHistogramList.size();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<Application, AgentHistogram> agentHistogramMap = new HashMap<>();


        Builder() {
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

        public void addAgentHistogram(AgentHistogramList addAgentHistogramList) {
            Objects.requireNonNull(addAgentHistogramList, "addAgentHistogramList");

            for (AgentHistogram agentHistogram : addAgentHistogramList.getAgentHistogramList()) {
                addAgentHistogram(agentHistogram);
            }
        }

        public void addAgentHistogram(AgentHistogram agentHistogram) {
            Objects.requireNonNull(agentHistogram, "agentHistogram");

            final String hostName = agentHistogram.getId();
            ServiceType serviceType = agentHistogram.getServiceType();

            Application agentId = new Application(hostName, serviceType);
            AgentHistogram findAgentHistogram = getAgentHistogram(agentId);
            findAgentHistogram.addTimeHistogram(agentHistogram.getTimeHistogram());
        }


        private AgentHistogram getAgentHistogram(Application agentId) {
            return agentHistogramMap.computeIfAbsent(agentId, k -> new AgentHistogram(agentId));
        }

        public AgentHistogramList build(Application application, List<ResponseTime> responseHistogramList) {
            for (ResponseTime responseTime : responseHistogramList) {
                for (Map.Entry<String, TimeHistogram> agentEntry : responseTime.getAgentHistogram()) {
                    TimeHistogram timeHistogram = agentEntry.getValue();

                    addAgentHistogram(agentEntry.getKey(), application.getServiceType(), timeHistogram);
                }
            }
            List<AgentHistogram> copy = List.copyOf(agentHistogramMap.values());
            return new AgentHistogramList(copy);
        }

        public AgentHistogramList build() {
            List<AgentHistogram> copy = List.copyOf(agentHistogramMap.values());
            return new AgentHistogramList(copy);
        }
    }
}
