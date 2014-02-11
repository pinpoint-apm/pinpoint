package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhn.pinpoint.common.ServiceType;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * @author emeroad
 */
public class ResponseHistogramTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Test
    public void testDeepCopy() throws Exception {
        ResponseHistogram original = new ResponseHistogram(ServiceType.TOMCAT);
        original.addSample((short) 1000, 100);


        ResponseHistogram copy = new ResponseHistogram(ServiceType.TOMCAT);
        Assert.assertEquals(copy.getFastCount(), 0);
        copy.add(original);
        Assert.assertEquals(original.getFastCount(), copy.getFastCount());

        copy.addSample((short) 1000, 100);
        Assert.assertEquals(original.getFastCount(), 100);
        Assert.assertEquals(copy.getFastCount(), 200);

    }

    @Test
    public void testJson() throws Exception {
        ResponseHistogram original = new ResponseHistogram(ServiceType.TOMCAT);
        original.addSample((short) 1000, 100);

        HashMap hashMap = objectMapper.readValue(original.getJson(), HashMap.class);

        Assert.assertEquals(hashMap.get("1000"), 100);
        Assert.assertEquals(hashMap.get("5000+"), 0);
    }
}
