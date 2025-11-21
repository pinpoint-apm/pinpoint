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

package com.navercorp.pinpoint.web.applicationmap.rawdata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import org.eclipse.collections.api.block.procedure.primitive.LongObjectProcedure;
import org.eclipse.collections.api.factory.primitive.LongObjectMaps;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author netspider
 * @author emeroad
 */
public class AgentHistogram {
    /**
     * to uniquely identify a host from UI, we can use things like hostname, agentId, endpoint, etc
     */
    private final Application agentId;
    private final List<TimeHistogram> timeHistogramList;

    AgentHistogram(Application agentId, List<TimeHistogram> timeHistogramList) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.timeHistogramList = Objects.requireNonNull(timeHistogramList, "timeHistogramList");
    }

    @JsonProperty("name")
    public String getId() {
        return agentId.getName();
    }

    @JsonIgnore
    public Application getAgentId() {
        return agentId;
    }

    @JsonIgnore
    public ServiceType getServiceType() {
        return agentId.getServiceType();
    }

    @JsonProperty("histogram")
    public Histogram getHistogram() {
        return Histogram.sumOf(agentId.getServiceType(), timeHistogramList);
    }

    @JsonIgnore
    public List<TimeHistogram> getTimeHistogram() {
        return timeHistogramList;
    }

    public static Builder newBuilder(Application agentId) {
        return new Builder(agentId);
    }

    public static class Builder {
        private final Application agentId;

        private final MutableLongObjectMap<TimeHistogram> timeHistogramMap;

        Builder(Application agentId) {
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            this.timeHistogramMap = LongObjectMaps.mutable.of();
        }

        public void addTimeHistogram(TimeHistogram timeHistogram) {
            TimeHistogram find = this.timeHistogramMap.get(timeHistogram.getTimeStamp());
            if (find == null) {
                find = new TimeHistogram(agentId.getServiceType(), timeHistogram.getTimeStamp());
                this.timeHistogramMap.put(timeHistogram.getTimeStamp(), find);
            }
            find.add(timeHistogram);
        }

        public void addTimeHistogram(Collection<TimeHistogram> histogramList) {
            Objects.requireNonNull(histogramList, "histogramList");

            for (TimeHistogram timeHistogram : histogramList) {
                addTimeHistogram(timeHistogram);
            }
        }

        public AgentHistogram build() {
            List<TimeHistogram> result = new ArrayList<>(timeHistogramMap.size());
            timeHistogramMap.forEachKeyValue((LongObjectProcedure<TimeHistogram>) (key, value) -> result.add(value));
            result.sort(TimeHistogram.TIME_STAMP_ASC_COMPARATOR);
            return new AgentHistogram(agentId, result);
        }
    }


    @Override
    public String toString() {
        return "AgentHistogram{" +
                "agent='" + agentId.getName() + '\'' +
                ", serviceType=" + agentId.getServiceType() +
                ", " + timeHistogramList +
                '}';
    }


}
