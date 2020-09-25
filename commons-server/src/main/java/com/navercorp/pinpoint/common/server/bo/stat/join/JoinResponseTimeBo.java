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
public class JoinResponseTimeBo implements JoinStatBo {

    public static final JoinResponseTimeBo EMPTY_JOIN_RESPONSE_TIME_BO = new JoinResponseTimeBo();
    public static final long UNCOLLECTED_VALUE = -1;


    private String id = UNKNOWN_ID;
    private long timestamp = Long.MIN_VALUE;
    private JoinLongFieldBo responseTimeJoinValue = JoinLongFieldBo.UNCOLLECTED_FIELD_BO;

    public JoinResponseTimeBo() {
    }

    public JoinResponseTimeBo(String id, long timestamp, long avg, long minAvg, String minAvgAgentId, long maxAvg, String maxAvgAgentId) {
        this(id, timestamp, new JoinLongFieldBo(avg, minAvg, minAvgAgentId, maxAvg, maxAvgAgentId));
    }

    public JoinResponseTimeBo(String id, long timestamp, JoinLongFieldBo responseTimeJoinValue) {
        this.id = id;
        this.timestamp = timestamp;
        this.responseTimeJoinValue = Objects.requireNonNull(responseTimeJoinValue, "responseTimeJoinValue");
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setResponseTimeJoinValue(JoinLongFieldBo responseTimeJoinValue) {
        this.responseTimeJoinValue = responseTimeJoinValue;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public JoinLongFieldBo getResponseTimeJoinValue() {
        return responseTimeJoinValue;
    }

    public static void apply(JoinApplicationStatBo.Builder builder, List<JoinResponseTimeBo> joinResponseTimeBoList, Long timestamp) {
        builder.addResponseTime(joinResponseTimeBoList(joinResponseTimeBoList, timestamp));
    }

    public static JoinResponseTimeBo joinResponseTimeBoList(List<JoinResponseTimeBo> joinResponseTimeBoList, Long timestamp) {
        if (joinResponseTimeBoList.isEmpty()) {
            return JoinResponseTimeBo.EMPTY_JOIN_RESPONSE_TIME_BO;
        }

        List<JoinLongFieldBo> responseTimeFieldBoList = joinResponseTimeBoList.stream().map(JoinResponseTimeBo::getResponseTimeJoinValue).collect(Collectors.toList());
        final JoinLongFieldBo responseTimeJoinValue = JoinLongFieldBo.merge(responseTimeFieldBoList);

        final JoinResponseTimeBo firstJoinResponseTimeBo = joinResponseTimeBoList.get(0);

        final JoinResponseTimeBo newJoinResponseTimeBo = new JoinResponseTimeBo();
        newJoinResponseTimeBo.setId(firstJoinResponseTimeBo.getId());
        newJoinResponseTimeBo.setTimestamp(timestamp);
        newJoinResponseTimeBo.setResponseTimeJoinValue(responseTimeJoinValue);

        return newJoinResponseTimeBo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JoinResponseTimeBo{");
        sb.append("id='").append(id).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", responseTimeJoinValue=").append(responseTimeJoinValue);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinResponseTimeBo that = (JoinResponseTimeBo) o;

        if (timestamp != that.timestamp) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return responseTimeJoinValue != null ? responseTimeJoinValue.equals(that.responseTimeJoinValue) : that.responseTimeJoinValue == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (responseTimeJoinValue != null ? responseTimeJoinValue.hashCode() : 0);
        return result;
    }

}
