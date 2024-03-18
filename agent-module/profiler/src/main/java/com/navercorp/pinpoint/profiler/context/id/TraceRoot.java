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
import com.navercorp.pinpoint.common.id.AgentId;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface TraceRoot extends LocalTraceRoot {

    TraceId getTraceId();

    @Override
    long getLocalTransactionId();

    @Override
    long getTraceStartTime();

    @Override
    Shared getShared();

    static TraceRoot remote(TraceId traceId, AgentId agentId, long traceStartTime, long localTransactionId) {
        return new RemoteTraceRootImpl(traceId, agentId, traceStartTime, localTransactionId);
    }

    static LocalTraceRoot local(AgentId agentId, long traceStartTime, long localTransactionId) {
        Objects.requireNonNull(agentId, "agentId");
        return new LocalTraceRootImpl(agentId, traceStartTime, localTransactionId);
    }
}
