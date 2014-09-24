package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.nhn.pinpoint.web.vo.Application;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author emeroad
 */
public class AgentHistogramTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ObjectMapper mapper = new ObjectMapper();
    @Test
    public void testDeepCopy() throws Exception {
        AgentHistogram agentHistogram = new AgentHistogram(new Application("test", ServiceType.TOMCAT));
        TimeHistogram histogram = new TimeHistogram(ServiceType.TOMCAT, 0);
        histogram.addCallCount(ServiceType.TOMCAT.getHistogramSchema().getErrorSlot().getSlotTime(), 1);
        agentHistogram.addTimeHistogram(histogram);

        AgentHistogram copy = new AgentHistogram(agentHistogram);
        Assert.assertEquals(copy.getHistogram().getErrorCount(), 1);

        TimeHistogram histogram2 = new TimeHistogram(ServiceType.TOMCAT, 0);
        histogram2.addCallCount(ServiceType.TOMCAT.getHistogramSchema().getErrorSlot().getSlotTime(), 2);
        agentHistogram.addTimeHistogram(histogram2);
        Assert.assertEquals(agentHistogram.getHistogram().getErrorCount(), 3);

        Assert.assertEquals(copy.getHistogram().getErrorCount(), 1);

    }

    @Test
    public void testJsonCompatibility() throws Exception {
        // json구현을 Jackson으로 변경시 호환성 테스트
        AgentHistogram agentHistogram = new AgentHistogram(new Application("test", ServiceType.TOMCAT));
        TimeHistogram histogram = new TimeHistogram(ServiceType.TOMCAT, 0);
        histogram.addCallCount(ServiceType.TOMCAT.getHistogramSchema().getErrorSlot().getSlotTime(), 1);
        agentHistogram.addTimeHistogram(histogram);

        AgentHistogram copy = new AgentHistogram(agentHistogram);
        logger.debug(copy.getHistogram().toString());
        Assert.assertEquals(copy.getHistogram().getErrorCount(), 1);

        TimeHistogram histogram2 = new TimeHistogram(ServiceType.TOMCAT, 0);
        histogram2.addCallCount(ServiceType.TOMCAT.getHistogramSchema().getErrorSlot().getSlotTime(), 2);
        agentHistogram.addTimeHistogram(histogram2);
        Assert.assertEquals(agentHistogram.getHistogram().getErrorCount(), 3);

        String callJson = mapper.writeValueAsString(agentHistogram);
        String before = originalJson(agentHistogram);
        logger.debug("callJson:{}", callJson);
        HashMap callJsonHashMap = mapper.readValue(callJson, HashMap.class);
        logger.debug("before:{}", before);
        HashMap beforeJsonHashMap = mapper.readValue(before, HashMap.class);
        logger.debug("{} {}", callJsonHashMap, beforeJsonHashMap);
        Assert.assertEquals(callJsonHashMap, beforeJsonHashMap);
    }


    public String originalJson(AgentHistogram agentHistogram) throws IOException {
        //이전구현
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"name\":\"").append(agentHistogram.getId()).append("\",");
        String histogram = mapper.writeValueAsString(agentHistogram.getHistogram());
        sb.append("\"histogram\":").append(histogram);
        sb.append("}");
        return sb.toString();
    }
}
