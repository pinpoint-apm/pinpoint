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

package com.navercorp.pinpoint.web.vo.scatter;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.web.view.DotSerializer;

import java.util.Objects;

@JsonSerialize(using = DotSerializer.class)
public class Dot {
    public static final int EXCEPTION_NONE = 0;

    public static final int SUCCESS_STATE = 1;
    public static final int FAILED_STATE = 0;

    private final TransactionId transactionId;
    private final long acceptedTime;
    private final int elapsedTime;
    private final int exceptionCode;
    private final String agentId;

    /**
     * 
     * @param transactionId
     * @param acceptedTime
     * @param elapsedTime
     * @param exceptionCode 0 : success, 1 : error
     */
    public Dot(TransactionId transactionId, long acceptedTime, int elapsedTime, int exceptionCode, String agentId) {
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
        this.agentId = Objects.requireNonNull(agentId, "agentId");

        this.acceptedTime = acceptedTime;
        this.elapsedTime = elapsedTime;
        this.exceptionCode = exceptionCode;
    }

    public TransactionId getTransactionId() {
        return transactionId;
    }

    public String getTransactionIdAsString() {
        return TransactionIdUtils.formatString(transactionId);
    }

    public int getExceptionCode() {
        return exceptionCode;
    }

    /**
     * Simple stateCode used in the UI. May need to be fleshed out with state transitions in the future. 
     * 
     * @return
     */
    public int getSimpleExceptionCode() {
        if (getExceptionCode() == Dot.EXCEPTION_NONE) {
            // feels like a failure should be a value greater 1
            return Dot.SUCCESS_STATE;
        } else {
            return Dot.FAILED_STATE;
        }
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public long getAcceptedTime() {
        return acceptedTime;
    }

    public String getAgentId() {
        return agentId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(64);
        sb.append("Dot{");
        sb.append("transactionId=").append(getTransactionIdAsString());
        sb.append(", acceptedTime=").append(acceptedTime);
        sb.append(", elapsedTime=").append(elapsedTime);
        sb.append(", exceptionCode=").append(exceptionCode);
        sb.append(", agentId='").append(agentId).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
