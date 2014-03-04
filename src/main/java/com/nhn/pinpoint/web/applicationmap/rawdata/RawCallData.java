package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.vo.LinkKey;

import java.util.HashMap;
import java.util.Map;

/**
 * 호출관계의 양방향 데이터를 표현
 * @author emeroad
 */
public class RawCallData {

    private final String source;
    private final ServiceType sourceServiceType;

	private final String target;
	private final ServiceType targetServiceType;

	private final Map<Long, Histogram> targetHistogramTimeMap;

    public RawCallData(LinkKey linkKey) {
        if (linkKey == null) {
            throw new NullPointerException("linkKey must not be null");
        }
        this.source = linkKey.getFromApplication();
        this.sourceServiceType = linkKey.getFromServiceType();

        this.target = linkKey.getToApplication();
        this.targetServiceType = linkKey.getToServiceType();

        this.targetHistogramTimeMap = new HashMap<Long, Histogram>();
    }

	public Histogram getHistogram() {
        Histogram histogram = new Histogram(targetServiceType);
        for (Histogram copy : targetHistogramTimeMap.values()) {
            histogram.add(copy);
        }
        return histogram;
	}

    public void addCallData(long timestamp, short slot, long count) {
        Histogram histogram = targetHistogramTimeMap.get(timestamp);
        if (histogram == null) {
            histogram = new Histogram(targetServiceType);
            targetHistogramTimeMap.put(timestamp, histogram);
        }
        histogram.addCallCount(slot, count);
    }

    public void addRawCallData(RawCallData copyRawCallData) {
        if (copyRawCallData == null) {
            throw new NullPointerException("copyRawCallData must not be null");
        }
        for (Map.Entry<Long, Histogram> copyEntry : copyRawCallData.targetHistogramTimeMap.entrySet()) {
            Histogram histogram = targetHistogramTimeMap.get(copyEntry.getKey());
            if (histogram == null) {
                histogram = new Histogram(targetServiceType);
                targetHistogramTimeMap.put(copyEntry.getKey(), histogram);
            }
            histogram.add(copyEntry.getValue());
        }
    }
}
