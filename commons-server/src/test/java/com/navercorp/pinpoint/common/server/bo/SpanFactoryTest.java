package com.navercorp.pinpoint.common.server.bo;

import com.google.common.collect.Lists;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanFactoryTest {

    private final SpanFactory spanFactory = new SpanFactory();

    private SpanFactoryAssert spanFactoryAssert = new SpanFactoryAssert();

    private RandomTSpan random = new RandomTSpan();


    @Test
    public void newSpanBo() throws Exception {
        TSpan tSpan = random.randomTSpan();

        SpanBo spanBo = spanFactory.newSpanBo(tSpan);

        spanFactoryAssert.assertSpan(tSpan, spanBo);
    }


    @Test
    public void newSpanChunkBo() throws Exception {
        TSpanChunk tSpanChunk = random.randomTSpanChunk();

        SpanChunkBo spanChunkBo = spanFactory.newSpanChunkBo(tSpanChunk);

        spanFactoryAssert.assertSpanChunk(tSpanChunk, spanChunkBo);

    }

    @Test
    public void newSpanEventBo() throws Exception {
        TSpan tSpan = random.randomTSpan();
        SpanBo spanBo = spanFactory.newSpanBo(tSpan);

        TSpanEvent tSpanEvent = random.randomTSpanEvent((short) RandomUtils.nextInt(0, 100));
        SpanEventBo spanEventBo = spanFactory.newSpanEventBo(spanBo, tSpanEvent);

        spanFactoryAssert.assertSpanEvent(tSpanEvent, spanEventBo);

    }

    @Test
    public void buildSpanBo() throws Exception {
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
    public void buildSpanChunkBo() throws Exception {
        TSpanChunk tSpanChunk = random.randomTSpanChunk();
        TSpanEvent tSpanEvent1 = random.randomTSpanEvent((short)0);
        TSpanEvent tSpanEvent2 = random.randomTSpanEvent((short)1);
        TSpanEvent tSpanEvent3 = random.randomTSpanEvent((short)5);
        TSpanEvent tSpanEvent4 = random.randomTSpanEvent((short)2);
        tSpanChunk.setSpanEventList(Lists.newArrayList(tSpanEvent1, tSpanEvent2, tSpanEvent3, tSpanEvent4));

        SpanChunkBo spanChunkBo = spanFactory.buildSpanChunkBo(tSpanChunk);

        spanFactoryAssert.assertSpanChunk(tSpanChunk, spanChunkBo);

    }

}