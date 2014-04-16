package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nhn.pinpoint.web.applicationmap.histogram.Histogram;
import com.nhn.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.nhn.pinpoint.web.vo.Application;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author netspider
 * @author emeroad
 */

public class AgentHistogram {
	/**
	 * UI에서 호스트를 구분하기 위한 목적으로 hostname, agentid, endpoint등 구분할 수 있는 아무거나 넣으면 됨.
	 */
	private final Application agentId;

    private final Map<Long, TimeHistogram> timeHistogramMap;

	public AgentHistogram(Application agentId) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }

        this.agentId = agentId;
        this.timeHistogramMap = new HashMap<Long, TimeHistogram>();
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
    public String getId() {
		return agentId.getName();
	}

    @JsonIgnore
	public ServiceType getServiceType() {
		return agentId.getServiceType();
	}

    @JsonProperty("histogram")
	public Histogram getHistogram() {
        Histogram histogram = new Histogram(agentId.getServiceType());
        for (TimeHistogram timeHistogram : timeHistogramMap.values()) {
            histogram.add(timeHistogram);
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
        // 자료 구조가 변경되어 잠시 땜빵.
        sb.append(", ").append(timeHistogramMap);
        sb.append('}');
        return sb.toString();
    }


}
