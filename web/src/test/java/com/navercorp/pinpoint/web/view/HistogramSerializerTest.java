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

package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * @author emeroad
 */
public class HistogramSerializerTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void testSerialize() throws Exception {
        Histogram original = new Histogram(ServiceType.STAND_ALONE);
        HistogramSchema schema = original.getHistogramSchema();
        original.addCallCount(schema.getFastSlot().getSlotTime(), 1);
        original.addCallCount(schema.getNormalSlot().getSlotTime(), 2);
        original.addCallCount(schema.getSlowSlot().getSlotTime(), 3);
        original.addCallCount(schema.getVerySlowSlot().getSlotTime(), 4);
        original.addCallCount(schema.getNormalErrorSlot().getSlotTime(), 5);

        String jacksonJson = objectMapper.writeValueAsString(original);
        HashMap objectMapperHashMap = objectMapper.readValue(jacksonJson, HashMap.class);

        logger.debug(jacksonJson);

        String internalJson = internalJson(original);
        HashMap hashMap = objectMapper.readValue(internalJson, HashMap.class);

        Assert.assertEquals(objectMapperHashMap, hashMap);
    }

    /**
     * moved this testcase for testing the old version histogram with manually created json code
     * @param histogram
     * @return
     */
    public String internalJson(Histogram histogram) {
        HistogramSchema histogramSchema = histogram.getHistogramSchema();
        final StringBuilder sb = new StringBuilder(128);
        sb.append("{ ");

        appendSlotTimeAndCount(sb, histogramSchema.getFastSlot().getSlotName(), histogram.getFastCount());
        sb.append(", ");
        appendSlotTimeAndCount(sb, histogramSchema.getNormalSlot().getSlotName(), histogram.getNormalCount());
        sb.append(", ");
        appendSlotTimeAndCount(sb, histogramSchema.getSlowSlot().getSlotName(), histogram.getSlowCount());
        sb.append(", ");

        // very slow means 0, so should use slow
        appendSlotTimeAndCount(sb, histogramSchema.getVerySlowSlot().getSlotName(), histogram.getVerySlowCount());
        sb.append(", ");
        appendSlotTimeAndCount(sb, histogramSchema.getErrorSlot().getSlotName(), histogram.getTotalErrorCount());
        sb.append(" }");

        return sb.toString();
    }

    private void appendSlotTimeAndCount(StringBuilder sb, String slotTimeName, long count) {
        sb.append('"');
        sb.append(slotTimeName);
        sb.append('"');
        sb.append(":");
        sb.append(count);
    }
}
