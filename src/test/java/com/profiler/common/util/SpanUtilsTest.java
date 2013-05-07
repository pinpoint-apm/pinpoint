package com.profiler.common.util;

import com.profiler.common.dto2.thrift.AgentKey;
import com.profiler.common.dto2.thrift.Span;
import com.profiler.common.hbase.HBaseTables;

import junit.framework.Assert;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

/**
 *
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
        for (int i = 0; i < HBaseTables.AGENT_NAME_MAX_LEN; i++) {
            agentId += "1";
        }

        long time = System.currentTimeMillis();
        check(agentId, time);
    }

    @Test
    public void testGetTraceIndexRowKey3() throws Exception {
        String agentId = "";
        for (int i = 0; i < HBaseTables.AGENT_NAME_MAX_LEN + 1; i++) {
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
        span.setStartTime(l1);

        byte[] traceIndexRowKey = SpanUtils.getAgentIdTraceIndexRowKey(span.getAgentId(), span.getStartTime());

        String agentId = Bytes.toString(traceIndexRowKey, 0, 24).trim();
        Assert.assertEquals(agentId0, agentId);

        long time = Bytes.toLong(traceIndexRowKey, HBaseTables.AGENT_NAME_MAX_LEN);
        Assert.assertEquals(time, l1);
    }
}
