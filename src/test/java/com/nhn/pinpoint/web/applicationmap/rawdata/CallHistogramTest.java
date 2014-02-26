package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.ServiceType;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class CallHistogramTest {
    @Test
    public void testDeepCopy() throws Exception {
        CallHistogram callHistogram = new CallHistogram("test", ServiceType.TOMCAT);
        callHistogram.getHistogram().addCallCount(ServiceType.TOMCAT.getHistogramSchema().getErrorSlot().getSlotTime(), 1);

        CallHistogram copy = new CallHistogram(callHistogram);
        Assert.assertEquals(copy.getHistogram().getErrorCount(), 1);

        callHistogram.getHistogram().addCallCount(ServiceType.TOMCAT.getHistogramSchema().getErrorSlot().getSlotTime(), 2);
        Assert.assertEquals(callHistogram.getHistogram().getErrorCount(), 3);

        Assert.assertEquals(copy.getHistogram().getErrorCount(), 1);

    }
}
