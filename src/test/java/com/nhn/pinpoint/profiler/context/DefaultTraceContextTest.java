package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.common.util.TraceIdUtils;
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
        DefaultTraceID traceID = new DefaultTraceID(agent, agentStartTime, agentTransactionCount);

        String id = traceID.getId();
        logger.info("id={}", id);

        int agentIdIndex = id.indexOf(DefaultTraceID.AGENT_DELIMITER);
        String agentId = id.substring(0, agentIdIndex);
        Assert.assertEquals(agentId, agent);

        String ids = id.substring(agentIdIndex + 1, id.length());
        String[] strings = TraceIdUtils.parseTraceId(ids);
        long startTime = TraceIdUtils.parseMostId(strings);
        Assert.assertEquals(startTime, agentStartTime);

        long trasnactionCount = TraceIdUtils.parseLeastId(strings);
        Assert.assertEquals(agentTransactionCount, trasnactionCount);

    }
}
