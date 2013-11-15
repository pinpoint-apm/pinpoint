package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class ResponseHistogramTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testDeepCopy() throws Exception {
        ResponseHistogram original = new ResponseHistogram(ServiceType.TOMCAT);
        original.addSample((short) 1000, 100);


        ResponseHistogram copy = new ResponseHistogram(ServiceType.TOMCAT);
        copy.add(original);
        Assert.assertFalse(original.getValues() == copy.getValues());
        Assert.assertEquals(original.getValues()[0], copy.getValues()[0]);

        copy.addSample((short) 1000, 100);
        Assert.assertEquals(original.getValues()[0], 100);
        Assert.assertEquals(copy.getValues()[0], 200);

    }
}
