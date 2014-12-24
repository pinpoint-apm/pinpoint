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

import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricRegistry;
import com.navercorp.pinpoint.profiler.monitor.metric.RpcMetric;

import junit.framework.Assert;

import org.junit.Test;

public class MetricRegistryTest {

    @Test
    public void testSuccess() {
        MetricRegistry metricRegistry = new MetricRegistry(ServiceType.TOMCAT);
        RpcMetric rpcMetric = metricRegistry.getRpcMetric(ServiceType.HTTP_CLIENT);


    }

    @Test
    public void testFalse() {
        MetricRegistry metricRegistry = null;
        try {
            metricRegistry = new MetricRegistry(ServiceType.ARCUS);
            Assert.fail();
        } catch (Exception e) {
        }

        metricRegistry = new MetricRegistry(ServiceType.TOMCAT);
        try {
            metricRegistry.getRpcMetric(ServiceType.IBATIS);
            Assert.fail();
        } catch (Exception e) {
        }

    }

}