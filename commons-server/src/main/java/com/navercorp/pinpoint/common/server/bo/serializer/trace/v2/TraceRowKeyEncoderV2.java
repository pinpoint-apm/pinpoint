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
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributor;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TraceRowKeyEncoderV2 implements RowKeyEncoder<TransactionId> {

    public static final int AGENT_ID_MAX_LEN = PinpointConstants.AGENT_ID_MAX_LEN;
    public static final int DISTRIBUTE_HASH_SIZE = 1;

    private final RowKeyDistributor rowKeyDistributor;

    public TraceRowKeyEncoderV2(RowKeyDistributor rowKeyDistributor) {
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
    }

    public byte[] encodeRowKey(TransactionId transactionId) {
        Objects.requireNonNull(transactionId, "transactionId");

        byte[] rowKey = BytesUtils.stringLongLongToBytes(DISTRIBUTE_HASH_SIZE, transactionId.getAgentId(), AGENT_ID_MAX_LEN, transactionId.getAgentStartTime(), transactionId.getTransactionSequence());
        byte prefix = this.rowKeyDistributor.getByteHasher().getHashPrefix(rowKey, DISTRIBUTE_HASH_SIZE);
        rowKey[0] = prefix;
        return rowKey;
    }

}
