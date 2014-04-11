package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.applicationmap.histogram.TimeHistogram;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author emeroad
 */
public class CallHistogramTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ObjectMapper mapper = new ObjectMapper();
    @Test
    public void testDeepCopy() throws Exception {
        CallHistogram callHistogram = new CallHistogram("test", ServiceType.TOMCAT);
        TimeHistogram histogram = new TimeHistogram(ServiceType.TOMCAT, 0);
        histogram.addCallCount(ServiceType.TOMCAT.getHistogramSchema().getErrorSlot().getSlotTime(), 1);
        callHistogram.addTimeHistogram(histogram);

        CallHistogram copy = new CallHistogram(callHistogram);
        Assert.assertEquals(copy.getHistogram().getErrorCount(), 1);

        TimeHistogram histogram2 = new TimeHistogram(ServiceType.TOMCAT, 0);
        histogram2.addCallCount(ServiceType.TOMCAT.getHistogramSchema().getErrorSlot().getSlotTime(), 2);
        callHistogram.addTimeHistogram(histogram2);
        Assert.assertEquals(callHistogram.getHistogram().getErrorCount(), 3);

        Assert.assertEquals(copy.getHistogram().getErrorCount(), 1);

    }

    @Test
    public void testJsonCompatibility() throws Exception {
        // json구현을 Jackson으로 변경시 호환성 테스트
        CallHistogram callHistogram = new CallHistogram("test", ServiceType.TOMCAT);
        TimeHistogram histogram = new TimeHistogram(ServiceType.TOMCAT, 0);
        histogram.addCallCount(ServiceType.TOMCAT.getHistogramSchema().getErrorSlot().getSlotTime(), 1);
        callHistogram.addTimeHistogram(histogram);

        CallHistogram copy = new CallHistogram(callHistogram);
        logger.debug(copy.getHistogram().toString());
        Assert.assertEquals(copy.getHistogram().getErrorCount(), 1);

        TimeHistogram histogram2 = new TimeHistogram(ServiceType.TOMCAT, 0);
        histogram2.addCallCount(ServiceType.TOMCAT.getHistogramSchema().getErrorSlot().getSlotTime(), 2);
        callHistogram.addTimeHistogram(histogram2);
        Assert.assertEquals(callHistogram.getHistogram().getErrorCount(), 3);

        String callJson = mapper.writeValueAsString(callHistogram);
        String before = originalJson(callHistogram);
        logger.debug("callJson:{}", callJson);
        HashMap callJsonHashMap = mapper.readValue(callJson, HashMap.class);
        logger.debug("before:{}", before);
        HashMap beforeJsonHashMap = mapper.readValue(before, HashMap.class);
        logger.debug("{} {}", callJsonHashMap, beforeJsonHashMap);
        Assert.assertEquals(callJsonHashMap, beforeJsonHashMap);
    }


    public String originalJson(CallHistogram callHistogram) throws IOException {
        //이전구현
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"name\":\"").append(callHistogram.getId()).append("\",");
        String histogram = mapper.writeValueAsString(callHistogram.getHistogram());
        sb.append("\"histogram\":").append(histogram);
        sb.append("}");
        return sb.toString();
    }
}
