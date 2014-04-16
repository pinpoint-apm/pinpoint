package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.applicationmap.histogram.Histogram;
import com.nhn.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.nhn.pinpoint.web.vo.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class AgentHistogramList {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    // agent별 Time 시리즈 데이터를 가지고 있음.
    private final Map<Application, AgentHistogram> agentHistogramMap = new HashMap<Application, AgentHistogram>();

    public AgentHistogramList() {
    }


    public void addCallHistogram(Application agentId, Collection<TimeHistogram> histogramList) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (histogramList == null) {
            throw new NullPointerException("histogramList must not be null");
        }
        AgentHistogram agentHistogram = getAgentHistogram(agentId);
        agentHistogram.addTimeHistogram(histogramList);
    }

    public void addCallHistogram(Application agentId, TimeHistogram timeHistogram) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (timeHistogram == null) {
            throw new NullPointerException("timeHistogram must not be null");
        }
        AgentHistogram agentHistogram = getAgentHistogram(agentId);
        agentHistogram.addTimeHistogram(timeHistogram);
    }

    public void addAgentHistogram(String agentName, ServiceType serviceType, Collection<TimeHistogram> histogramList) {
        Application agentId = new Application(agentName, serviceType);
        addCallHistogram(agentId, histogramList);
    }

    public void addAgentHistogram(String agentName, ServiceType serviceType, TimeHistogram timeHistogram) {
        Application agentId = new Application(agentName, serviceType);
        addCallHistogram(agentId, timeHistogram);
    }



    private AgentHistogram getAgentHistogram(Application agentId) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }

        AgentHistogram agentHistogram = agentHistogramMap.get(agentId);
        if (agentHistogram == null) {
            agentHistogram = new AgentHistogram(agentId);
            agentHistogramMap.put(agentId, agentHistogram);
        }
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
            throw new NullPointerException("agentHistogram must not be null");
        }
        final String hostName = agentHistogram.getId();
        ServiceType serviceType = agentHistogram.getServiceType();

        Application agentId = new Application(hostName, serviceType);
        AgentHistogram findAgentHistogram = getAgentHistogram(agentId);
        findAgentHistogram.addTimeHistogram(agentHistogram.getTimeHistogram());
    }

    public void addAgentHistogram(AgentHistogramList addAgentHistogramList) {
        if (addAgentHistogramList == null) {
            throw new NullPointerException("agentHistogram must not be null");
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
