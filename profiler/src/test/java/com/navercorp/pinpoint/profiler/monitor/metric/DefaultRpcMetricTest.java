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

package com.navercorp.pinpoint.profiler.monitor.metric;

import com.navercorp.pinpoint.common.HistogramSchema;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.monitor.metric.DefaultRpcMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.HistogramSnapshot;

import junit.framework.Assert;

import org.junit.Test;

import java.util.List;


public class DefaultRpcMetricTest {

    @Test
    public void testAddResponseTime() throws Exception {

        HistogramSchema schema = ServiceType.HTTP_CLIENT.getHistogramSchema();
        DefaultRpcMetric metric = new DefaultRpcMetric(ServiceType.HTTP_CLIENT);
        metric.addResponseTime("test1", schema.getFastSlot().getSlotTime());

        metric.addResponseTime("test2", schema.getSlowSlot().getSlotTime());
        metric.addResponseTime("test2", schema.getSlowSlot().getSlotTime());

        metric.addResponseTime("test3", schema.getErrorSlot().getSlotTime());
        metric.addResponseTime("test3", schema.getErrorSlot().getSlotTime());
        metric.addResponseTime("test3", schema.getErrorSlot().getSlotTime());

        List<HistogramSnapshot> snapshotList = metric.createSnapshotList();
        Assert.assertEquals(snapshotList.size(), 3);

    }
}