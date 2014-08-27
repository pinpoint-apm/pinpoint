package com.nhn.pinpoint.web.vo;

import java.util.*;
import java.util.Map.Entry;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class TransactionMetadataQuery {

    private final List<QueryCondition> queryConditionList;

	public TransactionMetadataQuery() {
        this.queryConditionList = new ArrayList<QueryCondition>();
	}

	public void addQueryCondition(String transactionId, long collectorAcceptTime, int responseTime) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        TransactionId traceId = new TransactionId(transactionId);
        QueryCondition condition = new QueryCondition(traceId, collectorAcceptTime, responseTime);
        queryConditionList.add(condition);
	}


	public List<TransactionId> getTransactionIdList() {
		final List<TransactionId> result = new ArrayList<TransactionId>(queryConditionList.size());
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
			return "QueryCondition [transactionId=" + transactionId + ", collectorAcceptorTime=" + collectorAcceptorTime + ", responseTime=" + responseTime + "]";
		}
	}
}
