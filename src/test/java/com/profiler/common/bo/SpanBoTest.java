package com.profiler.common.bo;


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
        spanBo.setAgentId("agent");
        spanBo.setEndPoint("end");
        spanBo.setName("name");
        spanBo.setServiceName("serviceName");

        byte[] bytes = spanBo.writeValue();
        logger.debug("length:{}", bytes.length);

        SpanBo newSpanBo = new SpanBo();
        int i = newSpanBo.readValue(bytes, 0);
        logger.debug("length:{}", i);
        Assert.assertEquals(bytes.length, i);

    }
}
