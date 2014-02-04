package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.web.applicationmap.rawdata.*;
import com.nhn.pinpoint.web.applicationmap.rawdata.ResponseHistogram;

import java.util.HashMap;
import java.util.Map;

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
    // agentId 이 key임.
    private final Map<String, ResponseHistogram> responseHistogramMap = new HashMap<String, ResponseHistogram>();



    public RawResponseTime(String applicationName, short applicationServiceType, long timeSlot) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        this.applicationName = applicationName;
        this.applicationServiceType = applicationServiceType;
        this.timeSlot = timeSlot;
    }

    public ResponseHistogram getHistogram(String agentId) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        final ResponseHistogram responseHistogram = responseHistogramMap.get(agentId);
        if (responseHistogram != null) {
            return responseHistogram;
        }
        final ResponseHistogram newHistogram = new ResponseHistogram(applicationServiceType);
        responseHistogramMap.put(agentId, newHistogram);
        return newHistogram;
    }

    public ResponseHistogram getTotalResponseHistogram() {
        throw new UnsupportedOperationException();
    }
}
