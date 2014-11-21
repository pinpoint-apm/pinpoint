package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceId;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.Version;
import com.nhn.pinpoint.common.util.TransactionId;
import com.nhn.pinpoint.common.util.TransactionIdUtils;
import com.nhn.pinpoint.profiler.AgentInformation;
import com.nhn.pinpoint.profiler.util.RuntimeMXBeanUtils;
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

    @Test
    public void disableTrace() {
        DefaultTraceContext traceContext = new DefaultTraceContext();
        Trace trace = traceContext.disableSampling();
        Assert.assertNotNull(trace);
        Assert.assertFalse(trace.canSampled());

        traceContext.detachTraceObject();

    }

    @Test
    public void threadLocalBindTest() {
        AgentInformation agentInformation = new AgentInformation("d", "d", System.currentTimeMillis(), 123, "machineName", "127.0.0.1", ServiceType.TEST_STAND_ALONE.getCode(), Version.VERSION);

        DefaultTraceContext traceContext1 = new DefaultTraceContext();
        traceContext1.setAgentInformation(agentInformation);
        Assert.assertNotNull(traceContext1.newTraceObject());

        DefaultTraceContext traceContext2 = new DefaultTraceContext();
        traceContext2.setAgentInformation(agentInformation);
        Trace notExist = traceContext2.currentRawTraceObject();
        Assert.assertNull(notExist);

        Assert.assertNotNull(traceContext1.currentRawTraceObject());
        traceContext1.detachTraceObject();
        Assert.assertNull(traceContext1.currentRawTraceObject());


    }
}
