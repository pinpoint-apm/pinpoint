/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap.rawdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;

import org.junit.Assert;
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
        AgentHistogram agentHistogram = new AgentHistogram(new Application("test", ServiceType.STAND_ALONE));
        TimeHistogram histogram = new TimeHistogram(ServiceType.STAND_ALONE, 0);
        histogram.addCallCount(ServiceType.STAND_ALONE.getHistogramSchema().getFastErrorSlot().getSlotTime(), 1);
        agentHistogram.addTimeHistogram(histogram);

        AgentHistogram copy = new AgentHistogram(agentHistogram);
        Assert.assertEquals(copy.getHistogram().getTotalErrorCount(), 1);

        TimeHistogram histogram2 = new TimeHistogram(ServiceType.STAND_ALONE, 0);
        histogram2.addCallCount(ServiceType.STAND_ALONE.getHistogramSchema().getFastErrorSlot().getSlotTime(), 2);
        agentHistogram.addTimeHistogram(histogram2);
        Assert.assertEquals(agentHistogram.getHistogram().getTotalErrorCount(), 3);

        Assert.assertEquals(copy.getHistogram().getTotalErrorCount(), 1);

    }

    @Test
    public void testJsonCompatibility() throws Exception {
        // compatibility test for changing to Jackson
        AgentHistogram agentHistogram = new AgentHistogram(new Application("test", ServiceType.STAND_ALONE));
        TimeHistogram histogram = new TimeHistogram(ServiceType.STAND_ALONE, 0);
        histogram.addCallCount(ServiceType.STAND_ALONE.getHistogramSchema().getFastErrorSlot().getSlotTime(), 1);
        agentHistogram.addTimeHistogram(histogram);

        AgentHistogram copy = new AgentHistogram(agentHistogram);
        logger.debug(copy.getHistogram().toString());
        Assert.assertEquals(copy.getHistogram().getTotalErrorCount(), 1);

        TimeHistogram histogram2 = new TimeHistogram(ServiceType.STAND_ALONE, 0);
        histogram2.addCallCount(ServiceType.STAND_ALONE.getHistogramSchema().getFastErrorSlot().getSlotTime(), 2);
        agentHistogram.addTimeHistogram(histogram2);
        Assert.assertEquals(agentHistogram.getHistogram().getTotalErrorCount(), 3);

        String callJson = mapper.writeValueAsString(agentHistogram);
        String before = originalJson(agentHistogram);
        logger.debug("callJson:{}", callJson);
        HashMap callJsonHashMap = mapper.readValue(callJson, HashMap.class);
        logger.debug("BEFORE:{}", before);
        HashMap beforeJsonHashMap = mapper.readValue(before, HashMap.class);
        logger.debug("{} {}", callJsonHashMap, beforeJsonHashMap);
        Assert.assertEquals(callJsonHashMap, beforeJsonHashMap);
    }


    public String originalJson(AgentHistogram agentHistogram) throws IOException {
        //old implementation
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"name\":\"").append(agentHistogram.getId()).append("\",");
        String histogram = mapper.writeValueAsString(agentHistogram.getHistogram());
        sb.append("\"histogram\":").append(histogram);
        sb.append('}');
        return sb.toString();
    }
}
