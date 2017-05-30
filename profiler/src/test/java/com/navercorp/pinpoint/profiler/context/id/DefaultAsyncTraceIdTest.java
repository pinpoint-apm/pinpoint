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

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.profiler.context.id.DefaultAsyncTraceId;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultAsyncTraceIdTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void nextAsyncSequence() throws Exception {

        long agentStartTime = System.currentTimeMillis();
        String testAgentId = "testAgentId";
        TraceId traceId = new DefaultTraceId(testAgentId, agentStartTime, 0);
        TraceRoot traceRoot = new DefaultTraceRoot(traceId, testAgentId, agentStartTime + 10, 2);

        AsyncTraceId asyncTraceId = new DefaultAsyncTraceId(traceRoot, 0);

        Assert.assertEquals(asyncTraceId.nextAsyncSequence(), 1);
        Assert.assertEquals(asyncTraceId.nextAsyncSequence(), 2);
        Assert.assertEquals(asyncTraceId.nextAsyncSequence(), 3);
    }

//    @Test
    public void testShortOverflow() {

        int overflow = Short.MAX_VALUE;

        logger.debug("{}", (short)overflow);
        overflow++;
        logger.debug("{}", (short)overflow);
        overflow++;
        logger.debug("{}", (short)overflow);
    }

}