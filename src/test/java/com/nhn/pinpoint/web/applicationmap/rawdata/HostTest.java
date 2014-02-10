package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.ServiceType;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class HostTest {
    @Test
    public void testDeepCopy() throws Exception {
        Host host = new Host("test", ServiceType.TOMCAT);
        host.getHistogram().addSample(HistogramSchema.ERROR_SLOT.getSlotTime(), 1);

        Host copy = new Host(host);
        Assert.assertEquals(copy.getHistogram().getErrorCount(), 1);

        host.getHistogram().addSample(HistogramSchema.ERROR_SLOT.getSlotTime(), 2);
        Assert.assertEquals(host.getHistogram().getErrorCount(), 3);

        Assert.assertEquals(copy.getHistogram().getErrorCount(), 1);

    }
}
