/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.vo.scatter;

/**
 * @author Taejin Koo
 */
public class DotAgentInfo {

    private final String agentId;
    private final String transactionAgentId;
    private final long transactionAgentStartTime;

    public DotAgentInfo(Dot dot) {
        this(dot.getAgentId(), dot.getTransactionId().getAgentId(), dot.getTransactionId().getAgentStartTime());
    }

    public DotAgentInfo(String agentId, String transactionAgentId, long transactionAgentStartTime) {
        this.agentId = agentId;
        this.transactionAgentId = transactionAgentId;
        this.transactionAgentStartTime = transactionAgentStartTime;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getTransactionAgentId() {
        return transactionAgentId;
    }

    public long getTransactionAgentStartTime() {
        return transactionAgentStartTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DotAgentInfo that = (DotAgentInfo) o;

        if (agentId != null ? !agentId.equals(that.getAgentId()) : that.getAgentId() != null) {
            return false;
        }
        if (transactionAgentStartTime != that.getTransactionAgentStartTime()) {
            return false;
        }
        return transactionAgentId != null ? transactionAgentId.equals(that.getTransactionAgentId()) : that.getTransactionAgentId() == null;
    }

    @Override
    public int hashCode() {
        int result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (transactionAgentId != null ? transactionAgentId.hashCode() : 0);
        result = 31 * result + (int) (transactionAgentStartTime ^ (transactionAgentStartTime >>> 32));
        return result;
    }

}
