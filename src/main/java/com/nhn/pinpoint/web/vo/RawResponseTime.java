package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.web.applicationmap.rawdata.*;
import com.nhn.pinpoint.web.applicationmap.rawdata.ResponseHistogram;

import java.util.*;

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

    public List<ResponseHistogram> getResponseHistogramList() {
        return new ArrayList<ResponseHistogram>(responseHistogramMap.values());
    }

    @Override
    public String toString() {
        return "RawResponseTime{" +
                "applicationName='" + applicationName + '\'' +
                ", applicationServiceType=" + applicationServiceType +
                ", timeSlot=" + timeSlot +
                ", responseHistogramMap=" + responseHistogramMap +
                '}';
    }
}
