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

package com.navercorp.pinpoint.common.profiler.util;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.IdValidateUtils;

import java.util.Objects;

/**
 * @author emeroad
 */
public final class TransactionIdUtils {
    // value is displayed as html - should not use html syntax
    public static final String TRANSACTION_ID_DELIMITER = "^";
    public static final int NULL = -1;

    public static final byte VERSION = 0;
    private static final byte VERSION_SIZE = 1;

    private TransactionIdUtils() {
    }

    public static String formatString(TransactionId transactionId) {
        return formatString(transactionId.getAgentId(), transactionId.getAgentStartTime(), transactionId.getTransactionSequence());
    }

    public static String formatString(String agentId, long agentStartTime, long transactionSequence) {
        Objects.requireNonNull(agentId, "agentId");

        return agentId +
                TRANSACTION_ID_DELIMITER +
                agentStartTime +
                TRANSACTION_ID_DELIMITER +
                transactionSequence;
    }

    public static byte[] formatBytes(String agentId, long agentStartTime, long transactionSequence) {
        return writeTransactionId(agentId, agentStartTime, transactionSequence);
    }


    private static byte[] writeTransactionId(String agentId, long agentStartTime, long transactionSequence) {
        // agentId may be null
        // version + prefixed size + string + long + long
        final byte[] agentIdBytes = BytesUtils.toBytes(agentId);
        final int agentIdLength = ArrayUtils.getLength(agentIdBytes, NULL);
        final int zigZagAgentIdLength = BytesUtils.intToZigZag(agentIdLength);
        final int agentIdPrefixSize = BytesUtils.computeVar32Size(zigZagAgentIdLength);
        final int agentStartTimeSize = BytesUtils.computeVar64Size(agentStartTime);
        final int transactionIdSequenceSize = BytesUtils.computeVar64Size(transactionSequence);

        final int bufferSize = VERSION_SIZE + agentIdPrefixSize + ArrayUtils.getLength(agentIdBytes) +  agentStartTimeSize + transactionIdSequenceSize;

        final byte[] buffer = new byte[bufferSize];
        buffer[0] = VERSION;
        int offset = VERSION_SIZE;
        // write prefix String
        offset = BytesUtils.writeVar32(zigZagAgentIdLength, buffer, offset);
        if (agentIdBytes != null) {
            offset = BytesUtils.writeBytes(buffer, offset, agentIdBytes);
        }
        offset = BytesUtils.writeVar64(agentStartTime, buffer, offset);
        BytesUtils.writeVar64(transactionSequence, buffer, offset);
        return buffer;
    }

    public static TransactionId parseTransactionId(final String transactionId) {
        Objects.requireNonNull(transactionId, "transactionId");

        final int agentIdIndex = nextIndex(transactionId, 0);
        if (agentIdIndex == -1) {
            throw new IllegalArgumentException("agentIndex not found:" + transactionId);
        }
        if (agentIdIndex > PinpointConstants.AGENT_ID_MAX_LEN) {
            throw new IllegalArgumentException("invalid transactionId:" + transactionId);
        }
        if (!IdValidateUtils.checkId(transactionId, 0, agentIdIndex)) {
            throw new IllegalArgumentException("invalid transactionId:" + transactionId);
        }
        final String agentId = transactionId.substring(0, agentIdIndex);

        final int agentStartTimeIndex = nextIndex(transactionId, agentIdIndex + 1);
        if (agentStartTimeIndex == -1) {
            throw new IllegalArgumentException("agentStartTimeIndex not found:" + transactionId);
        }
        final long agentStartTime = parseLong(transactionId, agentIdIndex + 1, agentStartTimeIndex);

        int transactionSequenceIndex = nextIndex(transactionId, agentStartTimeIndex + 1);
        if (transactionSequenceIndex == -1) {
            // next index may not exist since default value does not have a delimiter after transactionSequence.
            // may need fixing when id spec changes
            transactionSequenceIndex = transactionId.length();
        }
        final long transactionSequence = parseLong(transactionId, agentStartTimeIndex + 1, transactionSequenceIndex);
        return TransactionId.of(agentId, agentStartTime, transactionSequence);
    }

    private static int nextIndex(String transactionId, int fromIndex) {
        return transactionId.indexOf(TRANSACTION_ID_DELIMITER, fromIndex);
    }

    private static long parseLong(String transactionId, int beginIndex, int endIndex) {
        final String longString = transactionId.substring(beginIndex, endIndex);
        try {
            return Long.parseLong(longString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("parseLong Error. " + longString + " transactionId:" + transactionId);
        }
    }
}
