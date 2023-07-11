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
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.common.server.util.json.TypeRef;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

/**
 * @author emeroad
 */
public class AgentHistogramTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectMapper mapper = Jackson.newMapper();
    @Test
    public void testDeepCopy() {
        AgentHistogram agentHistogram = new AgentHistogram(new Application("test", ServiceType.STAND_ALONE));
        TimeHistogram histogram = new TimeHistogram(ServiceType.STAND_ALONE, 0);
        histogram.addCallCount(ServiceType.STAND_ALONE.getHistogramSchema().getFastErrorSlot().getSlotTime(), 1);
        agentHistogram.addTimeHistogram(histogram);

        AgentHistogram copy = new AgentHistogram(agentHistogram);
        Assertions.assertEquals(copy.getHistogram().getTotalErrorCount(), 1);

        TimeHistogram histogram2 = new TimeHistogram(ServiceType.STAND_ALONE, 0);
        histogram2.addCallCount(ServiceType.STAND_ALONE.getHistogramSchema().getFastErrorSlot().getSlotTime(), 2);
        agentHistogram.addTimeHistogram(histogram2);
        Assertions.assertEquals(agentHistogram.getHistogram().getTotalErrorCount(), 3);

        Assertions.assertEquals(copy.getHistogram().getTotalErrorCount(), 1);

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
        Assertions.assertEquals(copy.getHistogram().getTotalErrorCount(), 1);

        TimeHistogram histogram2 = new TimeHistogram(ServiceType.STAND_ALONE, 0);
        histogram2.addCallCount(ServiceType.STAND_ALONE.getHistogramSchema().getFastErrorSlot().getSlotTime(), 2);
        agentHistogram.addTimeHistogram(histogram2);
        Assertions.assertEquals(agentHistogram.getHistogram().getTotalErrorCount(), 3);

        String callJson = mapper.writeValueAsString(agentHistogram);
        String before = originalJson(agentHistogram);
        logger.debug("callJson:{}", callJson);

        Map<String, Object> callJsonHashMap = mapper.readValue(callJson, TypeRef.map());
        logger.debug("BEFORE:{}", before);
        Map<String, Object> beforeJsonHashMap = mapper.readValue(before, TypeRef.map());
        logger.debug("{} {}", callJsonHashMap, beforeJsonHashMap);
        Assertions.assertEquals(callJsonHashMap, beforeJsonHashMap);
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
