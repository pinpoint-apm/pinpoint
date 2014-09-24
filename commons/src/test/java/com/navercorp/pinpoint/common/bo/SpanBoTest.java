package com.nhn.pinpoint.common.bo;


import com.nhn.pinpoint.common.ServiceType;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class SpanBoTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testVersion() {
        SpanBo spanBo = new SpanBo();
        check(spanBo, 0);
        check(spanBo, 254);
        check(spanBo, 255);
        try {
            check(spanBo, 256);
            Assert.fail();
        } catch (Exception e) {
        }


    }

    private void check(SpanBo spanBo, int v) {
        spanBo.setVersion(v);
        int version = spanBo.getVersion();

        Assert.assertEquals(v, version);
    }

    @Test
    public void serialize() {
        SpanBo spanBo = new SpanBo();
        spanBo.setAgentId("agentId");
        spanBo.setApplicationId("applicationId");
        spanBo.setEndPoint("end");
        spanBo.setRpc("rpc");

        spanBo.setParentSpanId(5);

        spanBo.setAgentStartTime(1);
        spanBo.setTraceAgentStartTime(2);
        spanBo.setTraceTransactionSequence(3);
        spanBo.setElapsed(4);
        spanBo.setStartTime(5);


        spanBo.setServiceType(ServiceType.BLOC);
        byte[] bytes = spanBo.writeValue();
        logger.info("length:{}", bytes.length);

        SpanBo newSpanBo = new SpanBo();
        int i = newSpanBo.readValue(bytes, 0);
        logger.info("length:{}", i);
        Assert.assertEquals(bytes.length, i);
        Assert.assertEquals(newSpanBo.getAgentId(), spanBo.getAgentId());
        Assert.assertEquals(newSpanBo.getApplicationId(), spanBo.getApplicationId());
        Assert.assertEquals(newSpanBo.getAgentStartTime(), spanBo.getAgentStartTime());
        Assert.assertEquals(newSpanBo.getElapsed(), spanBo.getElapsed());
        Assert.assertEquals(newSpanBo.getEndPoint(), spanBo.getEndPoint());
        Assert.assertEquals(newSpanBo.getErrCode(), spanBo.getErrCode());
        Assert.assertEquals(newSpanBo.getFlag(), spanBo.getFlag());

//        이건 serialize에서 안가져옴.
//        Assert.assertEquals(newSpanBo.getTraceAgentStartTime(), spanBo.getTraceAgentStartTime());
//        Assert.assertEquals(newSpanBo.getTraceTransactionSequence(), spanBo.getTraceTransactionSequence());
        Assert.assertEquals(newSpanBo.getParentSpanId(), spanBo.getParentSpanId());

        Assert.assertEquals(newSpanBo.getVersion(), spanBo.getVersion());


    }

    @Test
    public void serialize2() {
        SpanBo spanBo = new SpanBo();
        spanBo.setAgentId("agent");
        String service = createString(5);
        spanBo.setApplicationId(service);
        String endPoint = createString(127);
        spanBo.setEndPoint(endPoint);
        String rpc = createString(255);
        spanBo.setRpc(rpc);

        spanBo.setServiceType(ServiceType.BLOC);

        byte[] bytes = spanBo.writeValue();
        logger.info("length:{}", bytes.length);

        SpanBo newSpanBo = new SpanBo();
        int i = newSpanBo.readValue(bytes, 0);
        logger.info("length:{}", i);
        Assert.assertEquals(bytes.length, i);
    }

    private String createString(int size) {
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append('a');
        }
        return sb.toString();
    }

}
