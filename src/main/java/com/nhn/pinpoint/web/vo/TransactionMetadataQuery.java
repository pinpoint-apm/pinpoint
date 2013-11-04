package com.nhn.pinpoint.web.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class TransactionMetadataQuery {

	private final Map<QueryCondition, Object> queryConditions;

	public TransactionMetadataQuery() {
		queryConditions = new HashMap<TransactionMetadataQuery.QueryCondition, Object>();
	}

	public void addQueryCondition(String unparsedTraceId, long time, int responseTime) {
        TransactionId traceId = new TransactionId(unparsedTraceId);
        QueryCondition condition = new QueryCondition(traceId, time, responseTime);

		if (queryConditions.containsKey(condition)) {
			return;
		}

		queryConditions.put(condition, null);
	}

	public boolean isExists(String traceAgentId, long traceAgentStartTime, long traceTransactionId, long time, int responseTime) {
        TransactionId traceId = new TransactionId(traceAgentId, traceAgentStartTime, traceTransactionId);
        QueryCondition queryCondition = new QueryCondition(traceId, time, responseTime);
        return queryConditions.containsKey(queryCondition);
	}

	public List<TransactionId> getTraceIds() {
		Set<TransactionId> temp = new HashSet<TransactionId>(queryConditions.size());
		for (Entry<QueryCondition, Object> entry : queryConditions.entrySet()) {
			temp.add(entry.getKey().getTraceId());
		}
		return new ArrayList<TransactionId>(temp);
	}

	public int size() {
		return queryConditions.size();
	}

	@Override
	public String toString() {
		return queryConditions.toString();
	}

	public static class QueryCondition {
		private final TransactionId traceId;
		private final long time;
		private final int responseTime;

		public QueryCondition(TransactionId traceId, long time, int responseTime) {
			this.traceId = traceId;
			this.time = time;
			this.responseTime = responseTime;
		}

		public TransactionId getTraceId() {
			return traceId;
		}

		public long getTime() {
			return time;
		}

		public int getResponseTime() {
			return responseTime;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + responseTime;
			result = prime * result + (int) (time ^ (time >>> 32));
			result = prime * result + ((traceId == null) ? 0 : traceId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			QueryCondition other = (QueryCondition) obj;
			if (responseTime != other.responseTime)
				return false;
			if (time != other.time)
				return false;
			if (traceId == null) {
				if (other.traceId != null)
					return false;
			} else if (!traceId.equals(other.traceId))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "QueryCondition [traceId=" + traceId + ", time=" + time + ", responseTime=" + responseTime + "]";
		}
	}
}
