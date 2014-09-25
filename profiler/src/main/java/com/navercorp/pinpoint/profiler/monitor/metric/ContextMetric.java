package com.nhn.pinpoint.profiler.monitor.metric;

import com.nhn.pinpoint.common.ServiceType;

/**
 * @author emeroad
 */
public class ContextMetric {
    // 실제 WAS 응답속도.
    private final Histogram responseMetric;
    // 모르는 user의 속도.
    private final Histogram userHistogram;

    private final ServiceType contextServiceType;

    // 어떤놈이 호출했는지 아는 경우.
    private final AcceptHistogram acceptHistogram = new DefaultAcceptHistogram();

    public ContextMetric(ServiceType contextServiceType) {
        if (contextServiceType == null) {
            throw new NullPointerException("contextServiceType must not be null");
        }

        this.contextServiceType = contextServiceType;

        this.responseMetric = new LongAdderHistogram(contextServiceType);
        this.userHistogram = new LongAdderHistogram(contextServiceType);
    }

    public void addResponseTime(int millis) {
        this.responseMetric.addResponseTime(millis);
    }

    public void addAcceptHistogram(String parentApplicationName, short serviceType, int millis) {
        if (parentApplicationName == null) {
            throw new NullPointerException("parentApplicationName must not be null");
        }
        this.acceptHistogram.addResponseTime(parentApplicationName, serviceType, millis);
    }

    public void addUserAcceptHistogram(int millis) {
        this.userHistogram.addResponseTime(millis);
    }




}
