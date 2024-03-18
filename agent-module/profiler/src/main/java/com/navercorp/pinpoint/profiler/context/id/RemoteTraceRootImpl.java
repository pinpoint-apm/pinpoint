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
public class RemoteTraceRootImpl extends LocalTraceRootImpl implements TraceRoot {

    private final TraceId traceId;

    RemoteTraceRootImpl(TraceId traceId, AgentId agentId, long traceStartTime, long localTransactionId) {
        super(agentId, traceStartTime, localTransactionId);
        this.traceId = Objects.requireNonNull(traceId, "traceId");
    }

    @Override
    public TraceId getTraceId() {
        return traceId;
    }

    @Override
    public String toString() {
        return "RemoteTraceRootImpl{" +
                "traceId=" + traceId +
                ", agentId='" + agentId + '\'' +
                ", localTransactionId=" + localTransactionId +
                ", traceStartTime=" + traceStartTime +
                ", shared=" + shared +
                '}';
    }

}
