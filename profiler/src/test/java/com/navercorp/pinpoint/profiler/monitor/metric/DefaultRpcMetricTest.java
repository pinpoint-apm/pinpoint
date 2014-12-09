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