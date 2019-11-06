/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.monitor.metric.rpc.test;

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.monitor.metric.rpc.HistogramSnapshot;

import com.navercorp.pinpoint.profiler.monitor.metric.rpc.LongAdderHistogram;
import org.junit.Assert;
import org.junit.Test;


public class HistogramTest {

    @Test
    public void testAddResponseTime() throws Exception {
        HistogramSchema schema = ServiceType.STAND_ALONE.getHistogramSchema();
        LongAdderHistogram histogram = new LongAdderHistogram(ServiceType.STAND_ALONE);
        histogram.addResponseTime(1000, false);

        histogram.addResponseTime(3000, false);
        histogram.addResponseTime(3000, false);

        histogram.addResponseTime(5000, false);
        histogram.addResponseTime(5000, false);
        histogram.addResponseTime(5000, false);

        histogram.addResponseTime(6000, false);
        histogram.addResponseTime(6000, false);
        histogram.addResponseTime(6000, false);
        histogram.addResponseTime(6000, false);

        histogram.addResponseTime(schema.getFastSlot().getSlotTime(), true);
        histogram.addResponseTime(schema.getFastSlot().getSlotTime(), true);
        histogram.addResponseTime(schema.getFastSlot().getSlotTime(), true);
        histogram.addResponseTime(schema.getFastSlot().getSlotTime(), true);
        histogram.addResponseTime(schema.getFastSlot().getSlotTime(), true);


        HistogramSnapshot snapshot = histogram.createSnapshot();
        Assert.assertEquals(snapshot.getFastCount(), 1);
        Assert.assertEquals(snapshot.getNormalCount(), 2);
        Assert.assertEquals(snapshot.getSlowCount(), 3);
        Assert.assertEquals(snapshot.getVerySlowCount(), 4);
        Assert.assertEquals(snapshot.getFastErrorCount(), 5);
    }

}