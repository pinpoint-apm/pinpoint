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

import com.google.inject.Inject;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.profiler.context.module.AgentId;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultTraceRootFactory implements TraceRootFactory {

    private final String agentId;
    private final TraceIdFactory traceIdFactory;
    private final IdGenerator idGenerator;

    @Inject
    public DefaultTraceRootFactory(@AgentId String agentId, TraceIdFactory traceIdFactory, IdGenerator idGenerator) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (traceIdFactory == null) {
            throw new NullPointerException("traceIdFactory must not be null");
        }
        if (idGenerator == null) {
            throw new NullPointerException("idGenerator must not be null");
        }
        this.agentId = agentId;
        this.traceIdFactory = traceIdFactory;
        this.idGenerator = idGenerator;
    }

    @Override
    public TraceRoot newTraceRoot() {
        final long localTransactionId = idGenerator.nextTransactionId();
        final TraceId traceId = traceIdFactory.newTraceId(localTransactionId);
        final long startTime = traceStartTime();
        return new DefaultTraceRoot(traceId, this.agentId, startTime, localTransactionId);
    }

    private long traceStartTime() {
        return System.currentTimeMillis();
    }


    @Override
    public TraceRoot continueTraceRoot(TraceId traceId) {
        if (traceId == null) {
            throw new NullPointerException("traceId must not be null");
        }
        final long startTime = traceStartTime();
        final long continuedTransactionId = this.idGenerator.nextContinuedTransactionId();
        return new DefaultTraceRoot(traceId, this.agentId, startTime, continuedTransactionId);
    }
}
