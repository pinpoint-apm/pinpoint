/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.trace.ServiceType;

import com.navercorp.pinpoint.profiler.context.id.DefaultTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.id.DefaultTransactionIdEncoder;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TransactionIdEncoder;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class SpanChunkFactoryTest {

    private final String agentId = "agentId";
    private final long agentStartTime = System.currentTimeMillis();
    private final TransactionIdEncoder encoder = new DefaultTransactionIdEncoder(agentId, agentStartTime);

    @Test
    public void create() {

        SpanChunkFactory spanChunkFactory = new SpanChunkFactoryV1("applicationName", agentId, agentStartTime, ServiceType.STAND_ALONE, encoder);
        TraceRoot internalTraceId = newInternalTraceId();
        try {
            spanChunkFactory.create(internalTraceId, new ArrayList<SpanEvent>());
            Assert.fail();
        } catch (Exception ignored) {
        }
        // one spanEvent
        List<SpanEvent> spanEvents = new ArrayList<SpanEvent>();
        spanEvents.add(new SpanEvent(internalTraceId));
        spanChunkFactory.create(internalTraceId, spanEvents);

        // two spanEvent
        spanEvents.add(new SpanEvent(internalTraceId));
        spanChunkFactory.create(internalTraceId, spanEvents);

        // three
        spanEvents.add(new SpanEvent(internalTraceId));
        spanChunkFactory.create(internalTraceId, spanEvents);

    }

    private TraceRoot newInternalTraceId() {
        TraceId traceId = new DefaultTraceId(agentId, agentStartTime, 100);
        return new DefaultTraceRoot(traceId, agentId, agentStartTime, 0);
    }
}
