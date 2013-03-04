package com.profiler.common.bo;


import com.profiler.common.ServiceType;
import com.profiler.common.bo.SpanBo;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
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

        spanBo.setServiceType(ServiceType.BLOC);
        byte[] bytes = spanBo.writeValue();
        logger.info("length:{}", bytes.length);

        SpanBo newSpanBo = new SpanBo();
        int i = newSpanBo.readValue(bytes, 0);
        logger.info("length:{}", i);
        Assert.assertEquals(bytes.length, i);
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
