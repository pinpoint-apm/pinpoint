/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo.stat.join;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JoinTotalThreadCountBo implements JoinStatBo {
    public static final JoinTotalThreadCountBo EMPTY_TOTAL_THREAD_COUNT_BO = new JoinTotalThreadCountBo();
    public static final long UNCOLLECTED_VALUE = -1L;

    private String id = UNKNOWN_ID;
    private long timestamp = Long.MIN_VALUE;

    private JoinLongFieldBo totalThreadCountJoinValue = JoinLongFieldBo.UNCOLLECTED_FIELD_BO;

    public JoinTotalThreadCountBo() {
    }

    public JoinTotalThreadCountBo(String id, long timestamp, long avgTotalThreadCount,
                                  long minTotalThreadCount, String minTotalThreadCountAgentId,
                                  long maxTotalThreadCount, String maxTotalThreadCountAgentId) {
        this(id, timestamp, new JoinLongFieldBo(avgTotalThreadCount, minTotalThreadCount, minTotalThreadCountAgentId, maxTotalThreadCount, maxTotalThreadCountAgentId));
    }

    public JoinTotalThreadCountBo(String id, long timestamp, JoinLongFieldBo totalThreadCountJoinValue) {
        this.id = id;
        this.timestamp = timestamp;
        this.totalThreadCountJoinValue = Objects.requireNonNull(totalThreadCountJoinValue, "totalThreadCountJoinValue");
    }

    @Override
    public long getTimestamp() { return timestamp; }

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public JoinLongFieldBo getTotalThreadCountJoinValue() {
        return totalThreadCountJoinValue;
    }

    public void setTotalThreadCountJoinValue(JoinLongFieldBo totalThreadCountJoinValue) {
        this.totalThreadCountJoinValue = totalThreadCountJoinValue;
    }

    public static void apply(JoinApplicationStatBo.Builder builder, List<JoinTotalThreadCountBo> joinTotalThreadCountBoList, Long timestamp) {
        builder.addTotalThreadCount(joinTotalThreadCountBoList(joinTotalThreadCountBoList, timestamp));
    }

    public static JoinTotalThreadCountBo joinTotalThreadCountBoList(List<JoinTotalThreadCountBo> joinTotalThreadCountBoList, Long timestamp) {
        if (joinTotalThreadCountBoList.isEmpty()) {
            return JoinTotalThreadCountBo.EMPTY_TOTAL_THREAD_COUNT_BO;
        }

        final List<JoinLongFieldBo> totalThreadCountFieldBoList = joinTotalThreadCountBoList.stream().map(e -> e.getTotalThreadCountJoinValue()).collect(Collectors.toList());
        final JoinLongFieldBo totalThreadCountJoinValue = JoinLongFieldBo.merge(totalThreadCountFieldBoList);

        final JoinTotalThreadCountBo firstJoinTotalThreadCountBo = joinTotalThreadCountBoList.get(0);

        final JoinTotalThreadCountBo newJoinTotalThreadCountBo = new JoinTotalThreadCountBo();
        newJoinTotalThreadCountBo.setId(firstJoinTotalThreadCountBo.getId());
        newJoinTotalThreadCountBo.setTimestamp(timestamp);
        newJoinTotalThreadCountBo.setTotalThreadCountJoinValue(totalThreadCountJoinValue);

        return newJoinTotalThreadCountBo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JoinTotalThreadCountBo{");
        sb.append("id='").append(id).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", totalThreadCountJoinValue=").append(totalThreadCountJoinValue);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinTotalThreadCountBo that = (JoinTotalThreadCountBo) o;

        if (timestamp != that.timestamp) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return totalThreadCountJoinValue != null ? totalThreadCountJoinValue.equals(that.totalThreadCountJoinValue) : that.totalThreadCountJoinValue == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (totalThreadCountJoinValue != null ? totalThreadCountJoinValue.hashCode() : 0);
        return result;
    }
}
