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

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;

/**
 * @author emeroad
 */
// FIXME Remove Comparable interface
public class TransactionId implements Comparable<TransactionId> {
    public static final int AGENT_NAME_MAX_LEN = PinpointConstants.AGENT_NAME_MAX_LEN;
    public static final int DISTRIBUTE_HASH_SIZE = 1;

    protected final String agentId;
    protected final long agentStartTime;
    protected final long transactionSequence;

    public TransactionId(byte[] transactionId) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        if (transactionId.length < BytesUtils.LONG_LONG_BYTE_LENGTH + AGENT_NAME_MAX_LEN) {
            throw new IllegalArgumentException("invalid transactionId length:" + transactionId.length);
        }

        this.agentId = BytesUtils.toStringAndRightTrim(transactionId, 0, AGENT_NAME_MAX_LEN);
        this.agentStartTime = BytesUtils.bytesToLong(transactionId, AGENT_NAME_MAX_LEN);
        this.transactionSequence = BytesUtils.bytesToLong(transactionId, BytesUtils.LONG_BYTE_LENGTH + AGENT_NAME_MAX_LEN);
    }

    public TransactionId(byte[] transactionId, int offset) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        if (transactionId.length < BytesUtils.LONG_LONG_BYTE_LENGTH + AGENT_NAME_MAX_LEN + offset) {
            throw new IllegalArgumentException("invalid transactionId length:" + transactionId.length);
        }

        this.agentId = BytesUtils.toStringAndRightTrim(transactionId, offset, AGENT_NAME_MAX_LEN);
        this.agentStartTime = BytesUtils.bytesToLong(transactionId, offset + AGENT_NAME_MAX_LEN);
        this.transactionSequence = BytesUtils.bytesToLong(transactionId, offset + BytesUtils.LONG_BYTE_LENGTH + AGENT_NAME_MAX_LEN);
    }

    public TransactionId(String agentId, long agentStartTime, long transactionSequence) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        this.agentId = agentId;
        this.agentStartTime = agentStartTime;
        this.transactionSequence = transactionSequence;
    }

    public TransactionId(String transactionId) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }

        com.navercorp.pinpoint.common.util.TransactionId parsedId = TransactionIdUtils.parseTransactionId(transactionId);
        this.agentId = parsedId.getAgentId();
        this.agentStartTime = parsedId.getAgentStartTime();
        this.transactionSequence = parsedId.getTransactionSequence();
    }

    public String getAgentId() {
        return agentId;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    public long getTransactionSequence() {
        return transactionSequence;
    }

    public byte[] getBytes() {
        return BytesUtils.stringLongLongToBytes(agentId, PinpointConstants.AGENT_NAME_MAX_LEN, agentStartTime, transactionSequence);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionId traceId = (TransactionId) o;

        if (agentStartTime != traceId.agentStartTime) return false;
        if (transactionSequence != traceId.transactionSequence) return false;
        if (!agentId.equals(traceId.agentId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = agentId.hashCode();
        result = 31 * result + (int) (agentStartTime ^ (agentStartTime >>> 32));
        result = 31 * result + (int) (transactionSequence ^ (transactionSequence >>> 32));
        return result;
    }

    @Override
    public String toString() {
        String traceId = TransactionIdUtils.formatString(agentId, agentStartTime, transactionSequence);
        return "TransactionId [" + traceId + "]";
    }

    public String getFormatString() {
        return TransactionIdUtils.formatString(agentId, agentStartTime, transactionSequence);
    }

    // FIXME remove
    @Override
    public int compareTo(TransactionId transactionId) {
        int r1 = this.agentId.compareTo(transactionId.agentId);
        if (r1 == 0) {
            if (this.agentStartTime > transactionId.agentStartTime) {
                return 1;
            } else if (this.agentStartTime < transactionId.agentStartTime) {
                return -1;
            } else {
                if (this.transactionSequence > transactionId.transactionSequence) {
                    return 1;
                } else if (this.transactionSequence < transactionId.transactionSequence) {
                    return -1;
                } else {
                    return 0;
                }
            }
        } else {
            return r1;
        }
    }
}
