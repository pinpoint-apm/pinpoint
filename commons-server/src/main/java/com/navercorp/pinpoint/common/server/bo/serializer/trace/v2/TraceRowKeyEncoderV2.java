/*
 * Copyright 2025 NAVER Corp.
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
import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributor;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TraceRowKeyEncoderV2 implements RowKeyEncoder<ServerTraceId> {

    public static final int AGENT_ID_MAX_LEN = PinpointConstants.AGENT_ID_MAX_LEN;
    public static final int OPENTELEMETRY_TRACE_ID_LEN = PinpointConstants.OPENTELEMETRY_TRACE_ID_LEN;

    private final ByteHasher byteHasher;

    public TraceRowKeyEncoderV2(RowKeyDistributor rowKeyDistributor) {
        Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
        this.byteHasher = rowKeyDistributor.getByteHasher();
    }

    public byte[] encodeRowKey(ServerTraceId transactionId) {
        return encodeRowKey(byteHasher.getSaltKey().size(), transactionId);
    }

    @Override
    public byte[] encodeRowKey(int saltKeySize, ServerTraceId serverTraceId) {
        Objects.requireNonNull(serverTraceId, "serverTraceId");
        if (!(serverTraceId instanceof PinpointServerTraceId pTraceId)) {
            throw new RuntimeException("invalid serverTraceId");
        }
        final String agentId = pTraceId.getAgentId();
        int maxStringSize = AGENT_ID_MAX_LEN;
        if(agentId.length() == OPENTELEMETRY_TRACE_ID_LEN) {
            maxStringSize = OPENTELEMETRY_TRACE_ID_LEN;
        }
        byte[] rowKey = RowKeyUtils.stringLongLongToBytes(saltKeySize, agentId, maxStringSize, pTraceId.getAgentStartTime(), pTraceId.getTransactionSequence());
        if (saltKeySize == 0) {
            return rowKey;
        }
        return byteHasher.writeSaltKey(rowKey);
    }
}
