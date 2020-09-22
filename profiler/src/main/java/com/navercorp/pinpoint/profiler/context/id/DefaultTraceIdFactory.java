/*
 * Copyright 2019 NAVER Corp.
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
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.context.module.config.ProfilerSamplingLiteRate;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultTraceIdFactory implements TraceIdFactory {

    private final String agentId;
    private final long agentStartTime;
    private final int samplingFullModeRate;
    private final int samplingLiteMinSeqNum;
    private final int samplingLiteOutOfNum;

    @Inject
    public DefaultTraceIdFactory(@AgentId String agentId, @AgentStartTime long agentStartTime, @ProfilerSamplingLiteRate int samplingLiteRate) {
        this.agentId = Assert.requireNonNull(agentId, "agentId");
        this.agentStartTime = agentStartTime;
        this.samplingFullModeRate = 100 - samplingLiteRate;
        boolean isPercent = (this.samplingFullModeRate % 10) != 0;
        if (isPercent) {
            this.samplingLiteOutOfNum = 100;
            this.samplingLiteMinSeqNum = this.samplingFullModeRate;
        } else {
            this.samplingLiteOutOfNum = 10;
            this.samplingLiteMinSeqNum = this.samplingFullModeRate / 10;
        }
    }

    @Override
    public TraceId newTraceId(long localTransactionId) {
        return new DefaultTraceId(agentId, agentStartTime, localTransactionId, getNewTraceFlags(localTransactionId));
    }

    private short getNewTraceFlags(long localTransactionId) {
        return isLiteTrace(localTransactionId) ? (short) 1 : 0;
    }

    private boolean isLiteTrace(long localTransactionId) {
        if (samplingFullModeRate == 100) {
            return false;
        }
        if (samplingFullModeRate == 0) {
            return true;
        }
        final long mod = localTransactionId % this.samplingLiteOutOfNum;
        return mod >= this.samplingLiteMinSeqNum;
    }

    public TraceId continueTraceId(String transactionId, long parentSpanId, long spanId, short flags) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId");
        }
        final TransactionId parseId = TransactionIdUtils.parseTransactionId(transactionId);
        return new DefaultTraceId(parseId.getAgentId(), parseId.getAgentStartTime(), parseId.getTransactionSequence(), parentSpanId, spanId, flags);
    }
}
