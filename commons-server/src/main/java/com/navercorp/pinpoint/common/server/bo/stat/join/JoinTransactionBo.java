/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.common.server.bo.stat.join;

import java.util.List;

/**
 * @author minwoo.jung
 */
public class JoinTransactionBo implements JoinStatBo {
    public static final JoinTransactionBo EMPTY_TRANSACTION_BO = new JoinTransactionBo();
    public static final long UNCOLLECTED_VALUE = -1;

    private String id = UNKNOWN_ID;
    private long collectInterval = UNCOLLECTED_VALUE;
    private long totalCount = UNCOLLECTED_VALUE;
    private String maxTotalCountAgentId = UNKNOWN_AGENT;
    private long maxTotalCount = UNCOLLECTED_VALUE;
    private String minTotalCountAgentId = UNKNOWN_AGENT;
    private long minTotalCount = UNCOLLECTED_VALUE;
    private long timestamp = Long.MIN_VALUE;

    public JoinTransactionBo() {
    }

    public JoinTransactionBo(String id, long collectInterval, long totalCount, long minTotalCount, String minTotalCountAgentId, long maxTotalCount, String maxTotalCountAgentId, long timestamp) {
        this.id = id;
        this.collectInterval = collectInterval;
        this.totalCount = totalCount;
        this.maxTotalCountAgentId = maxTotalCountAgentId;
        this.maxTotalCount = maxTotalCount;
        this.minTotalCountAgentId = minTotalCountAgentId;
        this.minTotalCount = minTotalCount;
        this.timestamp = timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getCollectInterval() {
        return collectInterval;
    }

    public void setCollectInterval(long collectInterval) {
        this.collectInterval = collectInterval;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public String getMaxTotalCountAgentId() {
        return maxTotalCountAgentId;
    }

    public void setMaxTotalCountAgentId(String maxTotalCountAgentId) {
        this.maxTotalCountAgentId = maxTotalCountAgentId;
    }

    public long getMaxTotalCount() {
        return maxTotalCount;
    }

    public void setMaxTotalCount(long maxTotalCount) {
        this.maxTotalCount = maxTotalCount;
    }

    public String getMinTotalCountAgentId() {
        return minTotalCountAgentId;
    }

    public void setMinTotalCountAgentId(String minTotalCountAgentId) {
        this.minTotalCountAgentId = minTotalCountAgentId;
    }

    public long getMinTotalCount() {
        return minTotalCount;
    }

    public void setMinTotalCount(long minTotalCount) {
        this.minTotalCount = minTotalCount;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static JoinTransactionBo joinTransactionBoLIst(List<JoinTransactionBo> joinTransactionBoList, Long timestamp) {
        final int boCount = joinTransactionBoList.size();

        if (boCount == 0) {
            return JoinTransactionBo.EMPTY_TRANSACTION_BO;
        }

        final JoinTransactionBo initJoinTransactionBo = joinTransactionBoList.get(0);
        long sumTotalCount = 0;
        String maxTotalCountAgentId = initJoinTransactionBo.getMaxTotalCountAgentId();
        long maxTotalCount = initJoinTransactionBo.getMaxTotalCount();
        String minTotalCountAgentId = initJoinTransactionBo.getMinTotalCountAgentId();
        long minTotalCount = initJoinTransactionBo.getMinTotalCount();

        for (JoinTransactionBo joinTransactionBo : joinTransactionBoList) {
            sumTotalCount += joinTransactionBo.getTotalCount();

            if (joinTransactionBo.getMaxTotalCount() > maxTotalCount) {
                maxTotalCount = joinTransactionBo.getMaxTotalCount();
                maxTotalCountAgentId = joinTransactionBo.getMaxTotalCountAgentId();
            }
            if (joinTransactionBo.getMinTotalCount() < minTotalCount) {
                minTotalCount = joinTransactionBo.getMinTotalCount();
                minTotalCountAgentId = joinTransactionBo.getMinTotalCountAgentId();
            }
        }

        final JoinTransactionBo newJoinTransactionBo = new JoinTransactionBo();
        newJoinTransactionBo.setId(initJoinTransactionBo.getId());
        newJoinTransactionBo.setTimestamp(timestamp);
        newJoinTransactionBo.setTotalCount(sumTotalCount / (long)boCount);
        newJoinTransactionBo.setCollectInterval(initJoinTransactionBo.getCollectInterval());
        newJoinTransactionBo.setMaxTotalCount(maxTotalCount);
        newJoinTransactionBo.setMaxTotalCountAgentId(maxTotalCountAgentId);
        newJoinTransactionBo.setMinTotalCount(minTotalCount);
        newJoinTransactionBo.setMinTotalCountAgentId(minTotalCountAgentId);

        return newJoinTransactionBo;
    }

    @Override
    public String toString() {
        return "JoinTransactionBo{" +
            "id='" + id + '\'' +
            ", timestamp=" + timestamp  +
            ", totalCount=" + totalCount +
            ", maxTotalCountAgentId='" + maxTotalCountAgentId + '\'' +
            ", maxTotalCount=" + maxTotalCount +
            ", minTotalCountAgentId='" + minTotalCountAgentId + '\'' +
            ", minTotalCount=" + minTotalCount +
            ", collectInterval=" + collectInterval +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinTransactionBo that = (JoinTransactionBo) o;

        if (collectInterval != that.collectInterval) return false;
        if (totalCount != that.totalCount) return false;
        if (maxTotalCount != that.maxTotalCount) return false;
        if (minTotalCount != that.minTotalCount) return false;
        if (timestamp != that.timestamp) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (maxTotalCountAgentId != null ? !maxTotalCountAgentId.equals(that.maxTotalCountAgentId) : that.maxTotalCountAgentId != null) return false;
        return minTotalCountAgentId != null ? minTotalCountAgentId.equals(that.minTotalCountAgentId) : that.minTotalCountAgentId == null;

    }
}
