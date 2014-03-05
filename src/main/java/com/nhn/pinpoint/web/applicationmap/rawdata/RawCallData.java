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


    @Deprecated
	public Histogram getHistogram() {
        final Histogram histogram = new Histogram(targetServiceType);
        for (Histogram copy : targetHistogramTimeMap.values()) {
            histogram.add(copy);
        }
        return histogram;
	}

    public Collection<TimeHistogram> getTimeHistogram() {
        return targetHistogramTimeMap.values();
    }

    public void addCallData(long timestamp, short slot, long count) {
        TimeHistogram histogram = targetHistogramTimeMap.get(timestamp);
        if (histogram == null) {
            histogram = new TimeHistogram(targetServiceType, timestamp);
            targetHistogramTimeMap.put(timestamp, histogram);
        }
        histogram.addCallCount(slot, count);
    }

    public void addRawCallData(RawCallData copyRawCallData) {
        if (copyRawCallData == null) {
            throw new NullPointerException("copyRawCallData must not be null");
        }
        for (Map.Entry<Long, TimeHistogram> copyEntry : copyRawCallData.targetHistogramTimeMap.entrySet()) {
            final Long timeStamp = copyEntry.getKey();
            TimeHistogram histogram = targetHistogramTimeMap.get(timeStamp);
            if (histogram == null) {
                histogram = new TimeHistogram(targetServiceType, timeStamp);
                targetHistogramTimeMap.put(timeStamp, histogram);
            }
            histogram.add(copyEntry.getValue());
        }
    }
}
