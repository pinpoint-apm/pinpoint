/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;

/**
 * Trace row key layout for agent-instrumented spans.
 * <pre>
 * salt(saltKeySize) + agentId(24, right-padded) + agentStartTime(8) + transactionSequence(8)
 * </pre>
 * The layout is fixed-length, which lets {@link OtelTraceRowKeyCodec#matches(byte[], int)}
 * tell the two layouts apart by total length on read.
 *
 * @author Woonduk Kang(emeroad)
 */
public final class PinpointTraceRowKeyCodec {

    public static final int AGENT_ID_MAX_LEN = PinpointConstants.AGENT_ID_MAX_LEN;

    private PinpointTraceRowKeyCodec() {
    }

    public static byte[] encode(int saltKeySize, PinpointServerTraceId traceId) {
        return RowKeyUtils.stringLongLongToBytes(saltKeySize, traceId.getAgentId(), AGENT_ID_MAX_LEN,
                traceId.getAgentStartTime(), traceId.getTransactionSequence());
    }

    public static PinpointServerTraceId decode(byte[] rowKey, int saltKeySize) {
        String agentId = BytesUtils.toStringAndRightTrim(rowKey, saltKeySize, AGENT_ID_MAX_LEN);
        long agentStartTime = ByteArrayUtils.bytesToLong(rowKey, saltKeySize + AGENT_ID_MAX_LEN);
        long transactionSequence = ByteArrayUtils.bytesToLong(rowKey, saltKeySize + BytesUtils.LONG_BYTE_LENGTH + AGENT_ID_MAX_LEN);
        return new PinpointServerTraceId(agentId, agentStartTime, transactionSequence);
    }
}

