package com.navercorp.pinpoint.common.server.bo;

import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanFactoryTest {

    private static final int REPEAT_COUNT = 10;

    private final SpanFactory spanFactory = new SpanFactory();

    private SpanFactoryAssert spanFactoryAssert = new SpanFactoryAssert();

    private RandomTSpan random = new RandomTSpan();


    @Test
    public void testNewSpanBo() throws Exception {
        TSpan tSpan = random.randomTSpan();

        SpanBo spanBo = spanFactory.newSpanBo(tSpan);

        spanFactoryAssert.assertSpan(tSpan, spanBo);
    }


    @Test
    public void testNewSpanBo_N() throws Exception {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testNewSpanBo();
        }
    }


    @Test
    public void testNewSpanChunkBo() throws Exception {
        TSpanChunk tSpanChunk = random.randomTSpanChunk();

        SpanChunkBo spanChunkBo = spanFactory.newSpanChunkBo(tSpanChunk);

        spanFactoryAssert.assertSpanChunk(tSpanChunk, spanChunkBo);

    }

    @Test
    public void testNewSpanChunkBo_N() throws Exception {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testNewSpanChunkBo();
        }
    }

    @Test
    public void testNewSpanEventBo() throws Exception {

        TSpanEvent tSpanEvent = random.randomTSpanEvent((short) RandomUtils.nextInt(0, 100));
        SpanEventBo spanEventBo = spanFactory.buildSpanEventBo(tSpanEvent);

        spanFactoryAssert.assertSpanEvent(tSpanEvent, spanEventBo);

    }

    @Test
    public void testNewSpanEventBo_N() throws Exception {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testNewSpanEventBo();
        }
    }

    @Test
    public void testBuildSpanBo() throws Exception {
        TSpan tSpan = random.randomTSpan();
        TSpanEvent tSpanEvent1 = random.randomTSpanEvent((short)0);
        TSpanEvent tSpanEvent2 = random.randomTSpanEvent((short)1);
        TSpanEvent tSpanEvent3 = random.randomTSpanEvent((short)5);
        TSpanEvent tSpanEvent4 = random.randomTSpanEvent((short)2);
        tSpan.setSpanEventList(Lists.newArrayList(tSpanEvent1, tSpanEvent2, tSpanEvent3, tSpanEvent4));

        SpanBo spanBo = spanFactory.buildSpanBo(tSpan);

        spanFactoryAssert.assertSpan(tSpan, spanBo);

    }

    @Test
    public void testBuildSpanBo_N() throws Exception {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testBuildSpanBo();
        }
    }


    @Test
    public void testBuildSpanChunkBo() throws Exception {
        TSpanChunk tSpanChunk = random.randomTSpanChunk();
        TSpanEvent tSpanEvent1 = random.randomTSpanEvent((short)0);
        TSpanEvent tSpanEvent2 = random.randomTSpanEvent((short)1);
        TSpanEvent tSpanEvent3 = random.randomTSpanEvent((short)5);
        TSpanEvent tSpanEvent4 = random.randomTSpanEvent((short)2);
        tSpanChunk.setSpanEventList(Lists.newArrayList(tSpanEvent1, tSpanEvent2, tSpanEvent3, tSpanEvent4));

        SpanChunkBo spanChunkBo = spanFactory.buildSpanChunkBo(tSpanChunk);

        spanFactoryAssert.assertSpanChunk(tSpanChunk, spanChunkBo);

    }

    @Test
    public void testBuildSpanChunkBo_N() throws Exception {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testBuildSpanChunkBo();
        }
    }

    @Test
    public void testTransactionId_skip_agentId() throws Exception {
        TSpan tSpan = new TSpan();
        tSpan.setAgentId("agentId");
        byte[] transactionIdBytes = TransactionIdUtils.formatBytes(null, 1, 2);
        tSpan.setTransactionId(transactionIdBytes);

        SpanBo spanBo = spanFactory.newSpanBo(tSpan);
        TransactionId transactionId = spanBo.getTransactionId();

        Assert.assertEquals(transactionId.getAgentId(), "agentId");
        Assert.assertEquals(transactionId.getAgentStartTime(), 1);
        Assert.assertEquals(transactionId.getTransactionSequence(), 2);
    }

    @Test
    public void testTransactionId_include_agentId() throws Exception {
        TSpan tSpan = new TSpan();
        tSpan.setAgentId("agentId");
        byte[] transactionIdBytes = TransactionIdUtils.formatBytes("transactionAgentId", 1, 2);
        tSpan.setTransactionId(transactionIdBytes);

        SpanBo spanBo = spanFactory.newSpanBo(tSpan);
        TransactionId transactionId = spanBo.getTransactionId();

        Assert.assertEquals(transactionId.getAgentId(), "transactionAgentId");
        Assert.assertEquals(transactionId.getAgentStartTime(), 1);
        Assert.assertEquals(transactionId.getTransactionSequence(), 2);
    }

}