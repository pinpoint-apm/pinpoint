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

package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.springframework.stereotype.Component;

/**
 * @author Woonduk Kang(emeroad)
 */
@Component
public class TraceRowKeyDecoderV2 implements RowKeyDecoder<TransactionId> {

    public static final int AGENT_NAME_MAX_LEN = TraceRowKeyEncoderV2.AGENT_NAME_MAX_LEN;
    public static final int DISTRIBUTE_HASH_SIZE = TraceRowKeyEncoderV2.DISTRIBUTE_HASH_SIZE;

    private final int distributeHashSize;


    public TraceRowKeyDecoderV2() {
        this(DISTRIBUTE_HASH_SIZE);
    }

    public TraceRowKeyDecoderV2(int distributeHashSize) {
        this.distributeHashSize = distributeHashSize;
    }


    @Override
    public TransactionId decodeRowKey(byte[] rowkey) {
        if (rowkey == null) {
            throw new NullPointerException("rowkey");
        }

        return readTransactionId(rowkey, distributeHashSize);
    }

    private TransactionId readTransactionId(byte[] rowKey, int offset) {

        String agentId = BytesUtils.toStringAndRightTrim(rowKey, offset, AGENT_NAME_MAX_LEN);
        long agentStartTime = BytesUtils.bytesToLong(rowKey, offset + AGENT_NAME_MAX_LEN);
        long transactionSequence = BytesUtils.bytesToLong(rowKey, offset + BytesUtils.LONG_BYTE_LENGTH + AGENT_NAME_MAX_LEN);

        return new TransactionId(agentId, agentStartTime, transactionSequence);
    }

    // for test
    public TransactionId readTransactionId(byte[] rowKey) {
        return readTransactionId(rowKey, 0);
    }
}
