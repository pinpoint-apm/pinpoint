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
public class JoinMemoryBo implements JoinStatBo {
    public static final JoinMemoryBo EMPTY_JOIN_MEMORY_BO = new JoinMemoryBo();
    public static final long UNCOLLECTED_VALUE = -1;

    private String id = UNKNOWN_ID;
    private long timestamp = Long.MIN_VALUE;

    private JoinLongFieldBo heapUsedJoinValue = JoinLongFieldBo.UNCOLLECTED_FIELD_BO;
    private JoinLongFieldBo nonHeapUsedJoinValue = JoinLongFieldBo.UNCOLLECTED_FIELD_BO;

    public JoinMemoryBo() {
    }

    public JoinMemoryBo(String id, long timestamp, long heapUsed, long minHeapUsed, long maxHeapUsed, String minHeapAgentId, String maxHeapAgentId, long nonHeapUsed, long minNonHeapUsed, long maxNonHeapUsed, String minNonHeapAgentId, String maxNonHeapAgentId) {
        this(id, timestamp, new JoinLongFieldBo(heapUsed, minHeapUsed, minHeapAgentId, maxHeapUsed, maxHeapAgentId), new JoinLongFieldBo(nonHeapUsed, minNonHeapUsed, minNonHeapAgentId, maxNonHeapUsed, maxNonHeapAgentId));
    }

    public JoinMemoryBo(String id, long timestamp, JoinLongFieldBo heapUsedJoinValue, JoinLongFieldBo nonHeapUsedJoinValue) {
        this.id = id;
        this.timestamp = timestamp;
        this.heapUsedJoinValue = Objects.requireNonNull(heapUsedJoinValue, "heapUsedJoinValue");
        this.nonHeapUsedJoinValue = Objects.requireNonNull(nonHeapUsedJoinValue, "nonHeapUsedJoinValue");
    }

    public JoinLongFieldBo getHeapUsedJoinValue() {
        return heapUsedJoinValue;
    }

    public void setHeapUsedJoinValue(JoinLongFieldBo heapUsedJoinValue) {
        this.heapUsedJoinValue = heapUsedJoinValue;
    }

    public JoinLongFieldBo getNonHeapUsedJoinValue() {
        return nonHeapUsedJoinValue;
    }

    public void setNonHeapUsedJoinValue(JoinLongFieldBo nonHeapUsedJoinValue) {
        this.nonHeapUsedJoinValue = nonHeapUsedJoinValue;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static void apply(JoinApplicationStatBo.Builder builder, List<JoinMemoryBo> joinMemoryBoList, Long timestamp) {
        builder.addMemory(joinMemoryBoList(joinMemoryBoList, timestamp));
    }

    public static JoinMemoryBo joinMemoryBoList(List<JoinMemoryBo> joinMemoryBoList, Long timestamp) {
        if (joinMemoryBoList.isEmpty()) {
            return JoinMemoryBo.EMPTY_JOIN_MEMORY_BO;
        }

        List<JoinLongFieldBo> heapUsedFieldBoList = joinMemoryBoList.stream().map(JoinMemoryBo::getHeapUsedJoinValue).collect(Collectors.toList());
        final JoinLongFieldBo heapUsedJoinValue = JoinLongFieldBo.merge(heapUsedFieldBoList);

        List<JoinLongFieldBo> nonHeapUsedFieldBoList = joinMemoryBoList.stream().map(JoinMemoryBo::getNonHeapUsedJoinValue).collect(Collectors.toList());
        final JoinLongFieldBo nonHeapUsedJoinValue = JoinLongFieldBo.merge(nonHeapUsedFieldBoList);


        final JoinMemoryBo firstJoinMemoryBo = joinMemoryBoList.get(0);

        final JoinMemoryBo newJoinMemoryBo = new JoinMemoryBo();
        newJoinMemoryBo.setId(firstJoinMemoryBo.getId());
        newJoinMemoryBo.setTimestamp(timestamp);
        newJoinMemoryBo.setHeapUsedJoinValue(heapUsedJoinValue);
        newJoinMemoryBo.setNonHeapUsedJoinValue(nonHeapUsedJoinValue);
        return newJoinMemoryBo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JoinMemoryBo{");
        sb.append("id='").append(id).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", heapUsedJoinValue=").append(heapUsedJoinValue);
        sb.append(", nonHeapUsedJoinValue=").append(nonHeapUsedJoinValue);
        sb.append('}');
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinMemoryBo that = (JoinMemoryBo) o;

        if (timestamp != that.timestamp) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (heapUsedJoinValue != null ? !heapUsedJoinValue.equals(that.heapUsedJoinValue) : that.heapUsedJoinValue != null) return false;
        return nonHeapUsedJoinValue != null ? nonHeapUsedJoinValue.equals(that.nonHeapUsedJoinValue) : that.nonHeapUsedJoinValue == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (heapUsedJoinValue != null ? heapUsedJoinValue.hashCode() : 0);
        result = 31 * result + (nonHeapUsedJoinValue != null ? nonHeapUsedJoinValue.hashCode() : 0);
        return result;
    }

}
