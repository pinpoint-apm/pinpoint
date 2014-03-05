package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.vo.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class CallHistogramList {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    // agent별 Time 시리즈 데이터를 가지고 있음.
    private final Map<Application, CallHistogram> callHistogramMap = new HashMap<Application, CallHistogram>();

    public CallHistogramList() {
    }

    public CallHistogramList(CallHistogramList copyCallHistogramList) {
        if (copyCallHistogramList == null) {
            throw new NullPointerException("copyCallHistogramList must not be null");
        }

        for (Map.Entry<Application, CallHistogram> copyEntry : copyCallHistogramList.callHistogramMap.entrySet()) {
            Application copyKey = copyEntry.getKey();
            CallHistogram copyValue = new CallHistogram(copyEntry.getValue());
            this.callHistogramMap.put(copyKey, copyValue);
        }
    }


    public void addHost(String agentName, ServiceType serviceType, Collection<TimeHistogram> histogramList) {
        if (agentName == null) {
            throw new NullPointerException("agent must not be null");
        }
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }

        CallHistogram callHistogram = getCallHistogram(agentName, serviceType);
        callHistogram.addTimeHistogram(histogramList);
    }


    public void addHostUncheck(String hostName, ServiceType serviceType, Collection<TimeHistogram> histogram) {
        if (hostName == null) {
            throw new NullPointerException("histogram must not be null");
        }
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        CallHistogram callHistogram = getCallHistogram(hostName, serviceType);
        callHistogram.addTimeHistogramUncheckType(histogram);
    }


    private CallHistogram getCallHistogram(String agent, ServiceType serviceType) {
        Application agentId = new Application(agent, serviceType);
        CallHistogram callHistogram = callHistogramMap.get(agentId);
        if (callHistogram == null) {
            callHistogram = new CallHistogram(agent, serviceType);
            callHistogramMap.put(agentId, callHistogram);
        }
        return callHistogram;
    }


    public void addCallHistogram(CallHistogram callHistogram) {
        if (callHistogram == null) {
            throw new NullPointerException("callHistogram must not be null");
        }
        final String hostName = callHistogram.getId();
        ServiceType serviceType = callHistogram.getServiceType();

        CallHistogram findCallHistogram = getCallHistogram(hostName, serviceType);
        findCallHistogram.addTimeHistogram(callHistogram.getTimeHistogram());
    }

    public void addHostList(CallHistogramList addCallHistogramList) {
        if (addCallHistogramList == null) {
            throw new NullPointerException("callHistogram must not be null");
        }
        for (CallHistogram callHistogram : addCallHistogramList.callHistogramMap.values()) {
            addCallHistogram(callHistogram);
        }
    }

    public Collection<CallHistogram> getCallHistogramList() {
        return callHistogramMap.values();
    }

    @Deprecated
    public void put(CallHistogramList callHistogramList) {
        if (callHistogramList == null) {
            throw new NullPointerException("callHistogram must not be null");
        }
        // 이 메소드를 문제가 있음 put정책이 정확하지 않음.
        for (CallHistogram callHistogram : callHistogramList.callHistogramMap.values()) {
            final String hostName = callHistogram.getId();
            ServiceType serviceType = callHistogram.getServiceType();
            Application agentId = new Application(hostName, serviceType);
            final CallHistogram old = this.callHistogramMap.put(agentId, callHistogram);
            if (old != null) {
                logger.warn("old key exist. key:{}, new:{} old:{}", agentId, callHistogram, old);
            }
        }
    }

    @Override
    public String toString() {
        return "CallHistogramList{"
                    + callHistogramMap +
                '}';
    }
}
