package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.vo.LinkKey;

import java.util.*;

/**
 * 호출관계의 양방향 데이터를 표현
 * @author emeroad
 */
public class RawCallData {

    private final String source;
    private final ServiceType sourceServiceType;

	private final String target;
	private final ServiceType targetServiceType;

	private final Map<Long, TimeHistogram> targetHistogramTimeMap;

    public RawCallData(LinkKey linkKey) {
        if (linkKey == null) {
            throw new NullPointerException("linkKey must not be null");
        }
        this.source = linkKey.getFromApplication();
        this.sourceServiceType = linkKey.getFromServiceType();

        this.target = linkKey.getToApplication();
        this.targetServiceType = linkKey.getToServiceType();

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

    public void addRawCallData(RawCallData copyRawCallData) {
        if (copyRawCallData == null) {
            throw new NullPointerException("copyRawCallData must not be null");
        }
        if (!this.source.equals(copyRawCallData.source)) {
            throw new IllegalArgumentException("source not equals");
        }
        if (this.sourceServiceType != copyRawCallData.sourceServiceType) {
            throw new IllegalArgumentException("sourceServiceType not equals");
        }
        if (!this.target.equals(copyRawCallData.target)) {
            throw new IllegalArgumentException("target not equals");
        }
        if (this.targetServiceType != copyRawCallData.targetServiceType) {
            throw new IllegalArgumentException("targetServiceType not equals");
        }

        for (Map.Entry<Long, TimeHistogram> copyEntry : copyRawCallData.targetHistogramTimeMap.entrySet()) {
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
}
