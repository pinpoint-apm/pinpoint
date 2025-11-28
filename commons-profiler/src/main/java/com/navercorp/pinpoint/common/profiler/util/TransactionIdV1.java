/*
 * Copyright 2025 NAVER Corp.
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

import com.navercorp.pinpoint.common.util.IdValidateUtils;

import java.util.Objects;

/**
 * @author emeroad
 */
public class TransactionIdV1 implements TransactionId {

    public static TransactionIdV1 EMPTY_ID = new TransactionIdV1("EMPTY", 0, 0);

    private final String agentId;
    private final long agentStartTime;
    private final long transactionSequence;

    private String cache;

    TransactionIdV1(String agentId, long agentStartTime, long transactionSequence) {
        if (!IdValidateUtils.validateId(agentId)) {
            throw new IllegalArgumentException("invalid agentId " + agentId);
        }
        this.agentId = agentId;
        this.agentStartTime = agentStartTime;
        this.transactionSequence = transactionSequence;
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public long getAgentStartTime() {
        return agentStartTime;
    }

    @Override
    public long getTransactionSequence() {
        return transactionSequence;
    }

    @Override
    public String getId() {
        if (cache == null) {
            cache = TransactionIdUtils.formatString(agentId, agentStartTime, transactionSequence);
        }
        return cache;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        TransactionIdV1 that = (TransactionIdV1) o;
        return agentStartTime == that.agentStartTime && transactionSequence == that.transactionSequence && Objects.equals(agentId, that.agentId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(agentId);
        result = 31 * result + Long.hashCode(agentStartTime);
        result = 31 * result + Long.hashCode(transactionSequence);
        return result;
    }

    @Override
    public String toString() {
        return getId();
    }
}
