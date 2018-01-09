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

package com.navercorp.pinpoint.common.util;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;

import java.nio.ByteBuffer;

/**
 * @author emeroad
 */
public final class TransactionIdUtils {
    // value is displayed as html - should not use html syntax
    public static final String TRANSACTION_ID_DELIMITER = "^";
    public static final byte VERSION = 0;
    private static final byte VERSION_SIZE = 1;

    private TransactionIdUtils() {
    }

    public static String formatString(TransactionId transactionId) {
        return formatString(transactionId.getAgentId(), transactionId.getAgentStartTime(), transactionId.getTransactionSequence());
    }

    public static String formatString(String agentId, long agentStartTime, long transactionSequence) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append(agentId);
        sb.append(TRANSACTION_ID_DELIMITER);
        sb.append(agentStartTime);
        sb.append(TRANSACTION_ID_DELIMITER);
        sb.append(transactionSequence);
        return sb.toString();
    }

    public static byte[] formatBytes(String agentId, long agentStartTime, long transactionSequence) {
        final byte[] buffer = writeTransactionId(agentId, agentStartTime, transactionSequence);
        return buffer;
    }

    public static ByteBuffer formatByteBuffer(String agentId, long agentStartTime, long transactionSequence) {
        final byte[] buffer = writeTransactionId(agentId, agentStartTime, transactionSequence);
        return ByteBuffer.wrap(buffer);
    }

    private static byte[] writeTransactionId(String agentId, long agentStartTime, long transactionSequence) {
        // agentId may be null
        // version + prefixed size + string + long + long
        final byte[] agentIdBytes = BytesUtils.toBytes(agentId);
        final int agentIdLength = getLength(agentIdBytes);
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

    private static int getLength(byte[] bytes) {
        if (bytes == null) {
            return -1;
        }
        return bytes.length;
    }

    public static TransactionId parseTransactionId(final byte[] transactionId) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        final Buffer buffer = new FixedBuffer(transactionId);
        final byte version = buffer.readByte();
        if (version != VERSION) {
            throw new IllegalArgumentException("invalid Version");
        }

        final String agentId = buffer.readPrefixedString();
        final long agentStartTime = buffer.readVLong();
        final long transactionSequence = buffer.readVLong();
        if (agentId == null) {
            return new TransactionId(agentStartTime, transactionSequence);
        } else {
            return new TransactionId(agentId, agentStartTime,transactionSequence);
        }
    }

    public static TransactionId parseTransactionId(final String transactionId) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }

        final int agentIdIndex = nextIndex(transactionId, 0);
        if (agentIdIndex == -1) {
            throw new IllegalArgumentException("agentIndex not found:" + transactionId);
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
        return new TransactionId(agentId, agentStartTime, transactionSequence);
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
