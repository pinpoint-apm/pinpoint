package com.nhn.pinpoint.common.util;

import com.nhn.pinpoint.common.PinpointConstants;
import com.nhn.pinpoint.thrift.dto.TSpan;

import junit.framework.Assert;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

/**
 * @author emeroad
 */
public class SpanUtilsTest {
    @Test
    public void testGetTraceIndexRowKeyWhiteSpace() throws Exception {
        String agentId = "test test";
        long time = System.currentTimeMillis();
        check(agentId, time);
    }

    @Test
    public void testGetTraceIndexRowKey1() throws Exception {
        String agentId = "test";
        long time = System.currentTimeMillis();
        check(agentId, time);
    }

    @Test
    public void testGetTraceIndexRowKey2() throws Exception {
        String agentId = "";
        for (int i = 0; i < PinpointConstants.AGENT_NAME_MAX_LEN; i++) {
            agentId += "1";
        }

        long time = System.currentTimeMillis();
        check(agentId, time);
    }

    @Test
    public void testGetTraceIndexRowKey3() throws Exception {
        String agentId = "";
        for (int i = 0; i < PinpointConstants.AGENT_NAME_MAX_LEN + 1; i++) {
            agentId += "1";
        }

        long time = System.currentTimeMillis();
        try {
            check(agentId, time);
            Assert.fail();
        } catch (Exception e) {
        }
    }

    private void check(String agentId0, long l1) {
        TSpan span = new TSpan();
        span.setAgentId(agentId0);
        span.setStartTime(l1);

        byte[] traceIndexRowKey = SpanUtils.getAgentIdTraceIndexRowKey(span.getAgentId(), span.getStartTime());

        String agentId = Bytes.toString(traceIndexRowKey, 0, PinpointConstants.AGENT_NAME_MAX_LEN).trim();
        Assert.assertEquals(agentId0, agentId);

        long time = TimeUtils.recoveryTimeMillis(Bytes.toLong(traceIndexRowKey, PinpointConstants.AGENT_NAME_MAX_LEN));
        Assert.assertEquals(time, l1);
    }
}
