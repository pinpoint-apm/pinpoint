/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.id;

import com.navercorp.pinpoint.bootstrap.context.TraceId;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultTraceRootTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String agentId = "agentId";

    @Test
    public void getCompactTransactionId() throws Exception {
        TraceRoot traceRoot = getCompactLocalTraceId();

        ByteBuffer buf1 = traceRoot.getCompactTransactionId();
        ByteBuffer buf2 = traceRoot.getCompactTransactionId();
        logger.debug("buf1.remaining:{}", buf1.remaining());
        logger.debug("buf2.remaining:{}", buf2.remaining());

        Assert.assertEquals(buf1, buf2);

        ByteBuffer binaryBuf= traceRoot.getBinaryTransactionId();
        Assert.assertNotEquals(buf1, binaryBuf);
        logger.debug("binaryBuf.remaining:{}", binaryBuf.remaining());
    }

    @Test
    public void getUnCompactTransactionId() throws Exception {
        TraceRoot traceRoot = getBinaryLocalTraceId();

        ByteBuffer buf1 = traceRoot.getCompactTransactionId();
        ByteBuffer buf2 = traceRoot.getCompactTransactionId();
        logger.debug("buf1.remaining:{}", buf1.remaining());
        logger.debug("buf2.remaining:{}", buf2.remaining());

        Assert.assertEquals(buf1, buf2);

        ByteBuffer binaryBuf= traceRoot.getBinaryTransactionId();
        Assert.assertEquals(buf1, binaryBuf);
        logger.debug("binaryBuf.remaining:{}", binaryBuf.remaining());
    }


    private TraceRoot getCompactLocalTraceId() {
        final int transactionId = 0;
        TraceId traceId = new DefaultTraceId(agentId, System.currentTimeMillis(), transactionId);
        return new DefaultTraceRoot(traceId, agentId, System.currentTimeMillis(), transactionId);
    }

    private TraceRoot getBinaryLocalTraceId() {
        final int transactionId = 0;
        TraceId traceId = new DefaultTraceId(agentId, System.currentTimeMillis(), transactionId);
        return new DefaultTraceRoot(traceId, "agent2", System.currentTimeMillis(), transactionId);
    }


}