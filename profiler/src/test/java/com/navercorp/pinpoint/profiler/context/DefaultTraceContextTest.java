package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.bootstrap.context.TraceId;
import com.nhn.pinpoint.common.util.TransactionId;
import com.nhn.pinpoint.common.util.TransactionIdUtils;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class DefaultTraceContextTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void parseTest() {
        String agent= "test";
        long agentStartTime = System.currentTimeMillis();
        long agentTransactionCount = 10;
        TraceId traceID = new DefaultTraceId(agent, agentStartTime, agentTransactionCount);

        String id = traceID.getTransactionId();
        logger.info("id={}", id);

        TransactionId transactionid = TransactionIdUtils.parseTransactionId(id);

        Assert.assertEquals(transactionid.getAgentId(), agent);
        Assert.assertEquals(transactionid.getAgentStartTime(), agentStartTime);
        Assert.assertEquals(transactionid.getTransactionSequence(), agentTransactionCount);

    }
}
