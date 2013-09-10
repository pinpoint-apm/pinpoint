package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.common.util.TransactionIdUtils;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DefaultTraceContextTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void parseTest() {
        String agent= "test";
        long agentStartTime = System.currentTimeMillis();
        long agentTransactionCount = 10;
        DefaultTraceId traceID = new DefaultTraceId(agent, agentStartTime, agentTransactionCount);

        String id = traceID.getTransactionId();
        logger.info("id={}", id);

        String[] strings = TransactionIdUtils.parseTransactionId(id);

        Assert.assertEquals(strings[0], agent);
        Assert.assertEquals(Long.parseLong(strings[1]), agentStartTime);
        Assert.assertEquals(Long.parseLong(strings[2]), agentTransactionCount);

    }
}
