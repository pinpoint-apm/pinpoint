package com.nhn.pinpoint.web.vo;

import java.util.*;
import java.util.Map.Entry;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class TransactionMetadataQuery {

    private static final Object V = new Object();
	private final LinkedHashMap<QueryCondition, Object> queryConditions;
    private final List<QueryCondition> queryConditionList;

	public TransactionMetadataQuery() {
		this.queryConditions = new LinkedHashMap<QueryCondition, Object>();
        this.queryConditionList = new ArrayList<QueryCondition>();
	}

	public void addQueryCondition(String transactionId, long collectorAcceptTime, int responseTime) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        TransactionId traceId = new TransactionId(transactionId);
        QueryCondition condition = new QueryCondition(traceId, collectorAcceptTime, responseTime);

		if (queryConditions.containsKey(condition)) {
			return;
		}

		queryConditions.put(condition, V);
        queryConditionList.add(condition);
	}

	public boolean isExists(String traceAgentId, long traceAgentStartTime, long traceTransactionId, long collectorAcceptTime, int responseTime) {
        if (traceAgentId == null) {
            throw new NullPointerException("traceAgentId must not be null");
        }
        final TransactionId traceId = new TransactionId(traceAgentId, traceAgentStartTime, traceTransactionId);
        final QueryCondition queryCondition = new QueryCondition(traceId, collectorAcceptTime, responseTime);
        return queryConditions.containsKey(queryCondition);
	}

	public List<TransactionId> getTransactionIdList() {
		final List<TransactionId> result = new ArrayList<TransactionId>(queryConditions.size());
		for (Entry<QueryCondition, Object> entry : queryConditions.entrySet()) {
			result.add(entry.getKey().getTransactionId());
		}
		return result;
	}

	public int size() {
		return queryConditions.size();
	}

	@Override
	public String toString() {
		return queryConditions.toString();
	}

    public QueryCondition getIndex(int index) {
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
