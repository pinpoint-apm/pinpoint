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