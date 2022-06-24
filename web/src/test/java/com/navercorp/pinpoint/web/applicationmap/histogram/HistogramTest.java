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
import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

/**
 * @author emeroad
 */
public class HistogramTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void pingSlot() {
        Histogram histogram = new Histogram(ServiceType.STAND_ALONE);
        histogram.addCallCount(BaseHistogramSchema.NORMAL_SCHEMA.getPingSlot().getSlotTime(), 1);
        Assertions.assertEquals(1, histogram.getPingCount());

        Assertions.assertEquals(0, histogram.getErrorCount());
    }

    @Test
    public void maxSlot() {
        Histogram histogram = new Histogram(ServiceType.STAND_ALONE);
        histogram.addCallCount(BaseHistogramSchema.NORMAL_SCHEMA.getMaxStatSlot().getSlotTime(), 1000);
        Assertions.assertEquals(1000, histogram.getMaxElapsed());

        Assertions.assertEquals(0, histogram.getErrorCount());
    }

    @Test
    public void errorSlot() {
        Histogram histogram = new Histogram(ServiceType.STAND_ALONE);
        histogram.addCallCount(BaseHistogramSchema.NORMAL_SCHEMA.getErrorSlot().getSlotTime(), 1);
        Assertions.assertEquals(1, histogram.getErrorCount());
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
    public void testDeepCopy() throws Exception {
        Histogram original = new Histogram(ServiceType.STAND_ALONE);
        original.addCallCount((short) 1000, 100);


        Histogram copy = new Histogram(ServiceType.STAND_ALONE);
        Assertions.assertEquals(copy.getFastCount(), 0);
        copy.add(original);
        Assertions.assertEquals(original.getFastCount(), copy.getFastCount());

        copy.addCallCount((short) 1000, 100);
        Assertions.assertEquals(original.getFastCount(), 100);
        Assertions.assertEquals(copy.getFastCount(), 200);

    }

    @Test
    public void testJson() throws Exception {
        HistogramSchema schema = ServiceType.STAND_ALONE.getHistogramSchema();
        Histogram original = new Histogram(ServiceType.STAND_ALONE);
        original.addCallCount(schema.getFastSlot().getSlotTime(), 100);

        String json = objectMapper.writeValueAsString(original);
        HashMap<?, ?> hashMap = objectMapper.readValue(json, HashMap.class);

        Assertions.assertEquals(hashMap.get(schema.getFastSlot().getSlotName()), 100);
        Assertions.assertEquals(hashMap.get(schema.getErrorSlot().getSlotName()), 0);
    }
}
