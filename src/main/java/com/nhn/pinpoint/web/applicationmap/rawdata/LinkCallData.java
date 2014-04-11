package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.LinkKey;

import java.util.*;

/**
 * 호출관계의 양방향 데이터를 표현
 * @author emeroad
 */
public class LinkCallData {

    private final String source;
    private final ServiceType sourceServiceType;

	private final String target;
	private final ServiceType targetServiceType;

	private final Map<Long, TimeHistogram> targetHistogramTimeMap;

    public LinkCallData(LinkKey linkKey) {
        if (linkKey == null) {
            throw new NullPointerException("linkKey must not be null");
        }
        this.source = linkKey.getFromApplication();
        this.sourceServiceType = linkKey.getFromServiceType();

        this.target = linkKey.getToApplication();
        this.targetServiceType = linkKey.getToServiceType();

        this.targetHistogramTimeMap = new HashMap<Long, TimeHistogram>();
    }

    public LinkCallData(Application source, Application target) {
        if (source == null) {
            throw new NullPointerException("linkKey must not be null");
        }
        this.source = source.getName();
        this.sourceServiceType = source.getServiceType();

        this.target = target.getName();
        this.targetServiceType = target.getServiceType();

        this.targetHistogramTimeMap = new HashMap<Long, TimeHistogram>();
    }



    public String getSource() {
        return source;
    }

    public ServiceType getSourceServiceType() {
        return sourceServiceType;
    }

    public String getTarget() {
        return target;
    }

    public ServiceType getTargetServiceType() {
        return targetServiceType;
    }

    public Collection<TimeHistogram> getTimeHistogram() {
        return targetHistogramTimeMap.values();
    }

    public void addCallData(long timestamp, short slot, long count) {
        TimeHistogram histogram = getTimeHistogram(timestamp);
        histogram.addCallCount(slot, count);
    }

    public void addRawCallData(LinkCallData copyLinkCallData) {
        if (copyLinkCallData == null) {
            throw new NullPointerException("copyLinkCallData must not be null");
        }
        if (!this.source.equals(copyLinkCallData.source)) {
            throw new IllegalArgumentException("source not equals");
        }
        if (this.sourceServiceType != copyLinkCallData.sourceServiceType) {
            throw new IllegalArgumentException("sourceServiceType not equals");
        }
        if (!this.target.equals(copyLinkCallData.target)) {
            throw new IllegalArgumentException("target not equals");
        }
        if (this.targetServiceType != copyLinkCallData.targetServiceType) {
            throw new IllegalArgumentException("targetServiceType not equals");
        }

        for (Map.Entry<Long, TimeHistogram> copyEntry : copyLinkCallData.targetHistogramTimeMap.entrySet()) {
            final Long timeStamp = copyEntry.getKey();
            TimeHistogram histogram = getTimeHistogram(timeStamp);
            histogram.add(copyEntry.getValue());
        }
    }

    private TimeHistogram getTimeHistogram(Long timeStamp) {
        TimeHistogram histogram = targetHistogramTimeMap.get(timeStamp);
        if (histogram == null) {
            histogram = new TimeHistogram(targetServiceType, timeStamp);
            targetHistogramTimeMap.put(timeStamp, histogram);
        }
        return histogram;
    }

    @Override
    public String toString() {
        return "LinkCallData{" +
                "source='" + source + '\'' +
                ", sourceServiceType=" + sourceServiceType +
                ", target='" + target + '\'' +
                ", targetServiceType=" + targetServiceType +
                '}';
    }
}
