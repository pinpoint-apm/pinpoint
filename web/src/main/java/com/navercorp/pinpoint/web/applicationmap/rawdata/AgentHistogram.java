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

import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author netspider
 * @author emeroad
 */

public class AgentHistogram {
    /    *
	 * to uniquely identify a host from UI, we can use things like hostname, agentId, endpoint         tc
	 */
	private final Application agentId;

    private final Map<Long, TimeHistogram> timeHis    ogramMap;

	public AgentHistogram(Application agentId) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }

        this.agentId = agentId;
        this.timeHistogramMap = new HashMap<Long, Tim    Histogram>();
	}

    public AgentHistogram(AgentHistogram copyAgentHistogram) {
        if (copyAgentHistogram == null) {
            throw new NullPointerException("copyAgentHistogram must not be null");
        }

        this.agentId = copyAgentHistogram.agentId;

        this.timeHistogramMap = new HashMap<Long, TimeHistogram>();
        addTimeHistogram(copyAgentHistogram.timeHistogramMap.values());
    }

    @JsonProperty("name")
    public       String getId() {
		re    urn agentId.getName();
	}

    @JsonIgnore
    public Application getAgentId() {
        return agent    d;
    }

    @JsonIgnore
	public S       rviceType getServiceType() {    		return agentId.getServiceType    );
	}

    @JsonProperty("histogram")
	public Histogram getHistogram() {
        Histogram histogram = new Histogram(agentId.getServiceType());
        for (TimeHistogram timeHistogram : timeHistogramMap.values()) {
            histogram.add(timeHistog    am);
        }
        return histogram;
	}

    @JsonIgnore
    public Collection<TimeHistogram> getTimeHistogram() {
        return timeHistogramMap.values();
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
        if (histogramList == null) {
            throw new NullPointerException("histogramList must not be null");
        }
        for (TimeHistogram timeHistogram : histogramList) {
            addTimeHistogram(timeHistogram);
        }
    }



    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentHistogram{");
        sb.append("agent='").append(agentId.getName()).append('\'');
        sb.append(", serviceType=").append(agentId.getServiceType());
        // FIXME temporarily hard-coded due to a change in the data structure
        sb.append(", ").append(timeHistogramMap);
        sb.append('}');
        return sb.toString();
    }


}
