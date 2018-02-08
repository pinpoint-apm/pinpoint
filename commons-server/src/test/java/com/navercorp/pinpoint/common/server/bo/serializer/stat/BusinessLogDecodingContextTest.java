package com.navercorp.pinpoint.common.server.bo.serializer.stat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;

/**
 * Created by suny on 2018/2/6.
 */
public class BusinessLogDecodingContextTest {
    @InjectMocks
    BusinessLogDecodingContext businessLogDecodingContext;
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSetAgentId() throws Exception {
        businessLogDecodingContext.setAgentId("agentId");
        Assert.assertEquals("AgentId",businessLogDecodingContext.getAgentId(),"agentId");
    }

    @Test
    public void testSetBaseTimestamp() throws Exception {
        long timestamp = System.currentTimeMillis();
        businessLogDecodingContext.setBaseTimestamp(timestamp);
        Assert.assertEquals("baseTimestamp",businessLogDecodingContext.getBaseTimestamp(),timestamp);
    }

    @Test
    public void testSetTimestampDelta() throws Exception {
        businessLogDecodingContext.setTimestampDelta(10l);
        Assert.assertEquals("timestampdelta",businessLogDecodingContext.getTimestampDelta(),10l);
    }

}