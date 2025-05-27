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

package com.navercorp.pinpoint.web.applicationmap.histogram;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.common.server.util.json.TypeRef;
import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author emeroad
 */
public class HistogramTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectMapper objectMapper = Jackson.newMapper();

    @Test
    public void pingSlot() {
        Histogram histogram = new Histogram(ServiceType.STAND_ALONE);
        histogram.addCallCount(BaseHistogramSchema.NORMAL_SCHEMA.getPingSlot().getSlotTime(), 1);
        Assertions.assertEquals(1, histogram.getPingCount());

        Assertions.assertEquals(0, histogram.getTotalErrorCount());
    }

    @Test
    public void maxSlot() {
        Histogram histogram = new Histogram(ServiceType.STAND_ALONE);
        histogram.addCallCount(BaseHistogramSchema.NORMAL_SCHEMA.getMaxStatSlot().getSlotTime(), 1000);
        Assertions.assertEquals(1000, histogram.getMaxElapsed());

        Assertions.assertEquals(0, histogram.getTotalErrorCount());
    }

    @Test
    public void errorSlot() {
        Histogram histogram = new Histogram(ServiceType.STAND_ALONE);
        histogram.addCallCount(BaseHistogramSchema.NORMAL_SCHEMA.getSlowErrorSlot().getSlotTime(), 1);
        Assertions.assertEquals(1, histogram.getSlowErrorCount());
        Assertions.assertEquals(0, histogram.getSuccessCount());
    }

    @Test
    public void slowSlot() {
        Histogram histogram = new Histogram(ServiceType.STAND_ALONE);
        histogram.addCallCount(BaseHistogramSchema.NORMAL_SCHEMA.getSlowSlot().getSlotTime(), 1);
        Assertions.assertEquals(1, histogram.getSlowCount());
        Assertions.assertEquals(1, histogram.getSuccessCount());
    }

    @Test
    public void verySlowSlot() {
        Histogram histogram = new Histogram(ServiceType.STAND_ALONE);
        histogram.addCallCount(BaseHistogramSchema.NORMAL_SCHEMA.getVerySlowSlot().getSlotTime(), 1);
        Assertions.assertEquals(1, histogram.getVerySlowCount());
        Assertions.assertEquals(1, histogram.getSuccessCount());
    }

    @Test
    public void testDeepCopy() {
        Histogram original = new Histogram(ServiceType.STAND_ALONE);
        original.addCallCount((short) 1000, 100);


        Histogram copy = new Histogram(ServiceType.STAND_ALONE);
        Assertions.assertEquals(0, copy.getFastCount());
        copy.add(original);
        Assertions.assertEquals(original.getFastCount(), copy.getFastCount());

        copy.addCallCount((short) 1000, 100);
        Assertions.assertEquals(100, original.getFastCount());
        Assertions.assertEquals(200, copy.getFastCount());

    }

    @Test
    public void testJson() throws Exception {
        HistogramSchema schema = ServiceType.STAND_ALONE.getHistogramSchema();
        Histogram original = new Histogram(ServiceType.STAND_ALONE);
        original.addCallCount(schema.getFastSlot().getSlotTime(), 100);

        String json = objectMapper.writeValueAsString(original);
        Map<String, Object> map = objectMapper.readValue(json, TypeRef.map());

        Assertions.assertEquals(100, map.get(schema.getFastSlot().getSlotName()));
        Assertions.assertEquals(0, map.get(schema.getTotalErrorView().getSlotName()));
    }
}
