package com.nhn.pinpoint.profiler.monitor.metric;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.ServiceType;
import org.junit.Assert;
import org.junit.Test;


public class HistogramTest {

    @Test
    public void testAddResponseTime() throws Exception {
        HistogramSchema schema = ServiceType.TOMCAT.getHistogramSchema();
        Histogram histogram = new Histogram(ServiceType.TOMCAT);
        histogram.addResponseTime(1000);

        histogram.addResponseTime(3000);
        histogram.addResponseTime(3000);

        histogram.addResponseTime(5000);
        histogram.addResponseTime(5000);
        histogram.addResponseTime(5000);

        histogram.addResponseTime(6000);
        histogram.addResponseTime(6000);
        histogram.addResponseTime(6000);
        histogram.addResponseTime(6000);

        histogram.addResponseTime(schema.getErrorSlot().getSlotTime());
        histogram.addResponseTime(schema.getErrorSlot().getSlotTime());
        histogram.addResponseTime(schema.getErrorSlot().getSlotTime());
        histogram.addResponseTime(schema.getErrorSlot().getSlotTime());
        histogram.addResponseTime(schema.getErrorSlot().getSlotTime());


        HistogramSnapshot snapshot = histogram.createSnapshot();
        Assert.assertEquals(snapshot.getFastCount(), 1);
        Assert.assertEquals(snapshot.getNormalCount(), 2);
        Assert.assertEquals(snapshot.getSlowCount(), 3);
        Assert.assertEquals(snapshot.getVerySlowCount(), 4);
        Assert.assertEquals(snapshot.getErrorCount(), 5);
    }

}