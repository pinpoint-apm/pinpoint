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

import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.profiler.monitor.metric.rpc.MetricRegistry;
import com.navercorp.pinpoint.profiler.monitor.metric.rpc.RpcMetric;


import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MetricRegistryTest {
    private static final ServiceType ASYNC_HTTP_CLIENT = ServiceTypeFactory.of(9056, "ASYNC_HTTP_CLIENT", RECORD_STATISTICS);
    
    @Test
    public void testSuccess() {
        MetricRegistry metricRegistry = new MetricRegistry(ServiceType.STAND_ALONE);
        RpcMetric rpcMetric = metricRegistry.getRpcMetric(ASYNC_HTTP_CLIENT);
    }

    @Test
    public void testFalse() {
        Assertions.assertThrows(Exception.class, () -> {
            new MetricRegistry(ServiceType.UNKNOWN_DB);
        });

        Assertions.assertThrows(Exception.class, () -> {
            MetricRegistry metricRegistry = new MetricRegistry(ServiceType.STAND_ALONE);
            metricRegistry.getRpcMetric(ServiceType.ASYNC);
        });
    }

}