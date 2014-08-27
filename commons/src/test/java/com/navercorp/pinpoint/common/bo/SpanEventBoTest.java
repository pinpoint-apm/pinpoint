package com.nhn.pinpoint.common.bo;

import com.nhn.pinpoint.common.ServiceType;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class SpanEventBoTest {

    @Test
    public void testSerialize() throws Exception {
        SpanEventBo spanEventBo = new SpanEventBo();
        spanEventBo.setAgentId("test");
        spanEventBo.setAgentStartTime(1);
        spanEventBo.setDepth(3);
        spanEventBo.setDestinationId("testdest");
        spanEventBo.setEndElapsed(2);
        spanEventBo.setEndPoint("endpoint");

        spanEventBo.setNextSpanId(4);
        spanEventBo.setRpc("rpc");

        spanEventBo.setServiceType(ServiceType.TOMCAT);
        spanEventBo.setSpanId(12);
        spanEventBo.setStartElapsed(100);

        byte[] bytes = spanEventBo.writeValue();

        SpanEventBo newSpanEventBo = new SpanEventBo();
        int i = newSpanEventBo.readValue(bytes, 0);
        Assert.assertEquals(bytes.length, i);


        Assert.assertEquals(spanEventBo.getAgentId(), newSpanEventBo.getAgentId());
        Assert.assertEquals(spanEventBo.getAgentStartTime(), newSpanEventBo.getAgentStartTime());
        Assert.assertEquals(spanEventBo.getDepth(), newSpanEventBo.getDepth());
        Assert.assertEquals(spanEventBo.getDestinationId(), newSpanEventBo.getDestinationId());
        Assert.assertEquals(spanEventBo.getEndElapsed(), newSpanEventBo.getEndElapsed());
        Assert.assertEquals(spanEventBo.getEndPoint(), newSpanEventBo.getEndPoint());


        Assert.assertEquals(spanEventBo.getNextSpanId(), newSpanEventBo.getNextSpanId());
        Assert.assertEquals(spanEventBo.getRpc(), newSpanEventBo.getRpc());
        Assert.assertEquals(spanEventBo.getServiceType(), newSpanEventBo.getServiceType());
        Assert.assertEquals(spanEventBo.getStartElapsed(), newSpanEventBo.getStartElapsed());


        // 아래는 rowKeye에서 가져 오는값.
        spanEventBo.setSpanId(1);
        newSpanEventBo.setSpanId(1);
        Assert.assertEquals(spanEventBo.getSpanId(), newSpanEventBo.getSpanId());

        spanEventBo.setTraceTransactionSequence(1);
        newSpanEventBo.setTraceTransactionSequence(1);
        Assert.assertEquals(spanEventBo.getTraceTransactionSequence(), newSpanEventBo.getTraceTransactionSequence());

        spanEventBo.setTraceAgentStartTime(3);
        newSpanEventBo.setTraceAgentStartTime(3);
        Assert.assertEquals(spanEventBo.getTraceAgentStartTime(), newSpanEventBo.getTraceAgentStartTime());

        spanEventBo.setSequence((short) 3);
        newSpanEventBo.setSequence((short) 3);
        Assert.assertEquals(spanEventBo.getSequence(), newSpanEventBo.getSequence());
    }
}
