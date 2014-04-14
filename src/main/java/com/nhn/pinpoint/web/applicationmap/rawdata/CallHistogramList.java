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
            CallHistogram newCallHistogram = new CallHistogram(copyEntry.getValue());
            this.callHistogramMap.put(copyKey, newCallHistogram);
        }
    }


    public void addCallHistogram(String agentName, ServiceType serviceType, Collection<TimeHistogram> histogramList) {
        if (agentName == null) {
            throw new NullPointerException("agent must not be null");
        }
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        if (histogramList == null) {
            throw new NullPointerException("histogramList must not be null");
        }
        CallHistogram callHistogram = getCallHistogram(agentName, serviceType);
        callHistogram.addTimeHistogram(histogramList);
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

    public Histogram mergeHistogram(ServiceType serviceType) {
        final Histogram histogram = new Histogram(serviceType);
        for (CallHistogram callHistogram : getCallHistogramList()) {
            histogram.add(callHistogram.getHistogram());
        }
        return histogram;
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

    public void addCallHistogram(CallHistogramList addCallHistogramList) {
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

    @Override
    public String toString() {
        return "CallHistogramList{"
                    + callHistogramMap +
                '}';
    }
}
