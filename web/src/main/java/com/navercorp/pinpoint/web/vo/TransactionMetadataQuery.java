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

import java.util.ArrayList;
import java.util.List;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;

/**
 * @author netspider
 * @author emeroad
 */
public class TransactionMetadataQuery {

    private final List<QueryCondition> queryConditionList;

    public TransactionMetadataQuery() {
        this.queryConditionList = new ArrayList<>();
    }

    public void addQueryCondition(String transactionId, long collectorAcceptTime, int responseTime) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        TransactionId traceId = TransactionIdUtils.parseTransactionId(transactionId);
        QueryCondition condition = new QueryCondition(traceId, collectorAcceptTime, responseTime);
        queryConditionList.add(condition);
    }


    public List<TransactionId> getTransactionIdList() {
        final List<TransactionId> result = new ArrayList<>(queryConditionList.size());
        for (QueryCondition queryCondition : queryConditionList) {
            result.add(queryCondition.getTransactionId());
        }
        return result;
    }

    public int size() {
        return queryConditionList.size();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransactionMetadataQuery{");
        sb.append("queryConditionList=").append(queryConditionList);
        sb.append('}');
        return sb.toString();
    }

    public QueryCondition getQueryConditionByIndex(int index) {
        return queryConditionList.get(index);
    }

    public static final class QueryCondition {
        private final TransactionId transactionId;
        private final long collectorAcceptorTime;
        private final int responseTime;

        public QueryCondition(TransactionId transactionId, long collectorAcceptorTime, int responseTime) {
            if (transactionId == null) {
                throw new NullPointerException("transactionId must not be null");
            }
            this.transactionId = transactionId;
            this.collectorAcceptorTime = collectorAcceptorTime;
            this.responseTime = responseTime;
        }

        public TransactionId getTransactionId() {
            return transactionId;
        }

        public long getCollectorAcceptorTime() {
            return collectorAcceptorTime;
        }

        public int getResponseTime() {
            return responseTime;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            QueryCondition that = (QueryCondition) o;

            if (collectorAcceptorTime != that.collectorAcceptorTime) return false;
            if (responseTime != that.responseTime) return false;
            if (!transactionId.equals(that.transactionId)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = transactionId.hashCode();
            result = 31 * result + (int) (collectorAcceptorTime ^ (collectorAcceptorTime >>> 32));
            result = 31 * result + responseTime;
            return result;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("QueryCondition{");
            sb.append("transactionId=").append(transactionId);
            sb.append(", collectorAcceptorTime=").append(collectorAcceptorTime);
            sb.append(", responseTime=").append(responseTime);
            sb.append('}');
            return sb.toString();
        }
    }
}
