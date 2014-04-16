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
    private final Map<Application, AgentHistogram> callHistogramMap = new HashMap<Application, AgentHistogram>();

    public AgentHistogramList() {
    }


    public void addCallHistogram(Application agentId, Collection<TimeHistogram> histogramList) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (histogramList == null) {
            throw new NullPointerException("histogramList must not be null");
        }
        AgentHistogram agentHistogram = getCallHistogram(agentId);
        agentHistogram.addTimeHistogram(histogramList);
    }

    public void addCallHistogram(String agentName, ServiceType serviceType, Collection<TimeHistogram> histogramList) {
        Application agentId = new Application(agentName, serviceType);
        addCallHistogram(agentId, histogramList);
    }



    private AgentHistogram getCallHistogram(Application agentId) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }

        AgentHistogram agentHistogram = callHistogramMap.get(agentId);
        if (agentHistogram == null) {
            agentHistogram = new AgentHistogram(agentId);
            callHistogramMap.put(agentId, agentHistogram);
        }
        return agentHistogram;
    }

    public Histogram mergeHistogram(ServiceType serviceType) {
        final Histogram histogram = new Histogram(serviceType);
        for (AgentHistogram agentHistogram : getCallHistogramList()) {
            histogram.add(agentHistogram.getHistogram());
        }
        return histogram;
    }



    public void addCallHistogram(AgentHistogram agentHistogram) {
        if (agentHistogram == null) {
            throw new NullPointerException("agentHistogram must not be null");
        }
        final String hostName = agentHistogram.getId();
        ServiceType serviceType = agentHistogram.getServiceType();

        Application agentId = new Application(hostName, serviceType);
        AgentHistogram findAgentHistogram = getCallHistogram(agentId);
        findAgentHistogram.addTimeHistogram(agentHistogram.getTimeHistogram());
    }

    public void addCallHistogram(AgentHistogramList addAgentHistogramList) {
        if (addAgentHistogramList == null) {
            throw new NullPointerException("agentHistogram must not be null");
        }
        for (AgentHistogram agentHistogram : addAgentHistogramList.callHistogramMap.values()) {
            addCallHistogram(agentHistogram);
        }
    }

    public Collection<AgentHistogram> getCallHistogramList() {
        return callHistogramMap.values();
    }

    @Override
    public String toString() {
        return "AgentHistogramList{"
                    + callHistogramMap +
                '}';
    }
}
