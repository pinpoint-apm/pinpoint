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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author minwoo.jung
 */
public class JoinTransactionBo implements JoinStatBo {
    public static final JoinTransactionBo EMPTY_JOIN_TRANSACTION_BO = new JoinTransactionBo();
    public static final long UNCOLLECTED_VALUE = -1;

    private String id = UNKNOWN_ID;
    private long collectInterval = UNCOLLECTED_VALUE;
    private JoinLongFieldBo totalCountJoinValue = JoinLongFieldBo.UNCOLLECTED_FIELD_BO;
    private long timestamp = Long.MIN_VALUE;

    public JoinTransactionBo() {
    }

    public JoinTransactionBo(String id, long collectInterval, long totalCount, long minTotalCount, String minTotalCountAgentId, long maxTotalCount, String maxTotalCountAgentId, long timestamp) {
        this(id, collectInterval, new JoinLongFieldBo(totalCount, minTotalCount, minTotalCountAgentId, maxTotalCount, maxTotalCountAgentId), timestamp);
    }

    public JoinTransactionBo(String id, long collectInterval, JoinLongFieldBo totalCountJoinValue, long timestamp) {
        this.id = id;
        this.collectInterval = collectInterval;
        this.totalCountJoinValue = Objects.requireNonNull(totalCountJoinValue, "totalCountJoinValue");
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

    public JoinLongFieldBo getTotalCountJoinValue() {
        return totalCountJoinValue;
    }

    public void setTotalCountJoinValue(JoinLongFieldBo totalCountJoinValue) {
        this.totalCountJoinValue = totalCountJoinValue;
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

    public static void apply(JoinApplicationStatBo.Builder builder, List<JoinTransactionBo> joinTransactionBoList, Long timestamp) {
        builder.addTransaction(joinTransactionBoList(joinTransactionBoList, timestamp));
    }

    public static JoinTransactionBo joinTransactionBoList(List<JoinTransactionBo> joinTransactionBoList, Long timestamp) {
        if (joinTransactionBoList.isEmpty()) {
            return JoinTransactionBo.EMPTY_JOIN_TRANSACTION_BO;
        }

        List<JoinLongFieldBo> totalCountFieldBoList = joinTransactionBoList.stream().map(JoinTransactionBo::getTotalCountJoinValue).collect(Collectors.toList());
        final JoinLongFieldBo totalCountJoinValue = JoinLongFieldBo.merge(totalCountFieldBoList);

        final JoinTransactionBo firstJoinTransactionBo = joinTransactionBoList.get(0);

        final JoinTransactionBo newJoinTransactionBo = new JoinTransactionBo();
        newJoinTransactionBo.setId(firstJoinTransactionBo.getId());
        newJoinTransactionBo.setTimestamp(timestamp);
        newJoinTransactionBo.setCollectInterval(firstJoinTransactionBo.getCollectInterval());
        newJoinTransactionBo.setTotalCountJoinValue(totalCountJoinValue);
        return newJoinTransactionBo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinTransactionBo that = (JoinTransactionBo) o;

        if (collectInterval != that.collectInterval) return false;
        if (timestamp != that.timestamp) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return totalCountJoinValue != null ? totalCountJoinValue.equals(that.totalCountJoinValue) : that.totalCountJoinValue == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (collectInterval ^ (collectInterval >>> 32));
        result = 31 * result + (totalCountJoinValue != null ? totalCountJoinValue.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JoinTransactionBo{");
        sb.append("id='").append(id).append('\'');
        sb.append(", collectInterval=").append(collectInterval);
        sb.append(", totalCountJoinValue=").append(totalCountJoinValue);
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }

}
