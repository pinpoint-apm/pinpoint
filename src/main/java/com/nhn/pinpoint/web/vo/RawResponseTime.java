package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.web.applicationmap.rawdata.*;
import com.nhn.pinpoint.web.applicationmap.rawdata.ResponseHistogram;

/**
 * @author emeroad
 */
public class RawResponseTime {
    // rowKey
    private final String applicationName;
    private final short applicationServiceType;
    private final long timeSlot;

    // column
    // ex: test agent의 2슬롯 카운트는 10
    private final ResponseHistogram histogram;

    public RawResponseTime(String applicationName, short applicationServiceType, long timeSlot) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        this.applicationName = applicationName;
        this.applicationServiceType = applicationServiceType;
        this.timeSlot = timeSlot;
        this.histogram = new ResponseHistogram(applicationServiceType);
    }

    public ResponseHistogram getHistogram() {
        return histogram;
    }
}
