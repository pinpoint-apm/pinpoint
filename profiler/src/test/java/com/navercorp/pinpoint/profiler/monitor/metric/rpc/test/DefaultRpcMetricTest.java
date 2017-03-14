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

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;

import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.profiler.monitor.metric.rpc.DefaultRpcMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.rpc.HistogramSnapshot;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;


public class DefaultRpcMetricTest {
    private static final ServiceType ASYNC_HTTP_CLIENT = ServiceTypeFactory.of(9056, "ASYNC_HTTP_CLIENT", RECORD_STATISTICS);
    
    @Test
    public void testAddResponseTime() throws Exception {

        HistogramSchema schema = ASYNC_HTTP_CLIENT.getHistogramSchema();
        DefaultRpcMetric metric = new DefaultRpcMetric(ASYNC_HTTP_CLIENT);
        metric.addResponseTime("test1", schema.getFastSlot().getSlotTime(), false);

        metric.addResponseTime("test2", schema.getSlowSlot().getSlotTime(), false);
        metric.addResponseTime("test2", schema.getSlowSlot().getSlotTime(), false);

        metric.addResponseTime("test3", schema.getFastSlot().getSlotTime(), true);
        metric.addResponseTime("test3", schema.getFastSlot().getSlotTime(), true);
        metric.addResponseTime("test3", schema.getVerySlowSlot().getSlotTime(), true);

        List<HistogramSnapshot> snapshotList = metric.createSnapshotList();
        Assert.assertEquals(snapshotList.size(), 3);

    }
}