package com.profiler.common.util;

import com.profiler.common.dto.thrift.Span;
import junit.framework.Assert;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

/**
 *
 */
public class SpanUtilsTest {
    @Test
    public void testGetTraceIndexRowKey1() throws Exception {
        String agentId = "test";
        long time = System.currentTimeMillis();
        check(agentId, time);
    }

    @Test
    public void testGetTraceIndexRowKey2() throws Exception {
        String agentId = "";
        for (int i = 0; i < SpanUtils.AGENT_NAME_LIMIT; i++) {
            agentId += "1";
        }

        long time = System.currentTimeMillis();
        check(agentId, time);
    }

    @Test
    public void testGetTraceIndexRowKey3() throws Exception {
        String agentId = "";
        for (int i = 0; i < SpanUtils.AGENT_NAME_LIMIT + 1; i++) {
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
        Span span = new Span();
        span.setAgentId(agentId0);
        span.setTimestamp(l1);

        byte[] traceIndexRowKey = SpanUtils.getTraceIndexRowKey(span);

        String agentId = Bytes.toString(traceIndexRowKey, 0, agentId0.length());
        Assert.assertEquals(agentId0, agentId);

        long time = Bytes.toLong(traceIndexRowKey, SpanUtils.AGENT_NAME_LIMIT);
        Assert.assertEquals(time, l1);
    }
}
