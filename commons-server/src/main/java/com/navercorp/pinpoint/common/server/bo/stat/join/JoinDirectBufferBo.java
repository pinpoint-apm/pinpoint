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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Roy Kim
 */
public class JoinDirectBufferBo implements JoinStatBo {
    public static final JoinDirectBufferBo EMPTY_JOIN_DIRECT_BUFFER_BO = new JoinDirectBufferBo();
    public static final long UNCOLLECTED_VALUE = -1;

    private String id = UNKNOWN_ID;
    private long timestamp = Long.MIN_VALUE;

    private JoinLongFieldBo directCountJoinValue = JoinLongFieldBo.UNCOLLECTED_FIELD_BO;
    private JoinLongFieldBo directMemoryUsedJoinValue = JoinLongFieldBo.UNCOLLECTED_FIELD_BO;
    private JoinLongFieldBo mappedCountJoinValue = JoinLongFieldBo.UNCOLLECTED_FIELD_BO;
    private JoinLongFieldBo mappedMemoryUsedJoinValue = JoinLongFieldBo.UNCOLLECTED_FIELD_BO;

    public JoinDirectBufferBo() {
    }

    public JoinDirectBufferBo(String id, long avgDirectCount, long maxDirectCount, String maxDirectCountAgentId, long minDirectCount, String minDirectCountAgentId
            , long avgDirectMemoryUsed, long maxDirectMemoryUsed, String maxDirectMemoryUsedAgentId, long minDirectMemoryUsed, String minDirectMemoryUsedAgentId
            , long avgMappedCount, long maxMappedCount, String maxMappedCountAgentId, long minMappedCount, String minMappedCountAgentId
            , long avgMappedMemoryUsed, long maxMappedMemoryUsed, String maxMappedMemoryUsedAgentId, long minMappedMemoryUsed, String minMappedMemoryUsedAgentId
            , long timestamp) {
        this(id,
                new JoinLongFieldBo(avgDirectCount, minDirectCount, minDirectCountAgentId, maxDirectCount, maxDirectCountAgentId),
                new JoinLongFieldBo(avgDirectMemoryUsed, minDirectMemoryUsed, minDirectMemoryUsedAgentId, maxDirectMemoryUsed, maxDirectMemoryUsedAgentId),
                new JoinLongFieldBo(avgMappedCount, minMappedCount, minMappedCountAgentId, maxMappedCount, maxMappedCountAgentId),
                new JoinLongFieldBo(avgMappedMemoryUsed, minMappedMemoryUsed, minMappedMemoryUsedAgentId, maxMappedMemoryUsed, maxMappedMemoryUsedAgentId), timestamp);
    }


    public JoinDirectBufferBo(String id, JoinLongFieldBo directCountJoinValue, JoinLongFieldBo directMemoryUsedJoinValue,
                              JoinLongFieldBo mappedCountJoinValue, JoinLongFieldBo mappedMemoryUsedJoinValue, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
        this.directCountJoinValue = Objects.requireNonNull(directCountJoinValue, "directCountJoinValue");
        this.directMemoryUsedJoinValue = Objects.requireNonNull(directMemoryUsedJoinValue, "directMemoryUsedJoinValue");
        this.mappedCountJoinValue = Objects.requireNonNull(mappedCountJoinValue, "mappedCountJoinValue");
        this.mappedMemoryUsedJoinValue = Objects.requireNonNull(mappedMemoryUsedJoinValue, "mappedMemoryUsedJoinValue");
    }


    public static void apply(JoinApplicationStatBo.Builder builder, List<JoinDirectBufferBo> joinDirectBufferBoList, Long timestamp) {
        builder.addDirectBuffer(joinDirectBufferBoList(joinDirectBufferBoList, timestamp));
    }

    public static JoinDirectBufferBo joinDirectBufferBoList(List<JoinDirectBufferBo> joinDirectBufferBoList, Long timestamp) {
        if (joinDirectBufferBoList.isEmpty()) {
            return EMPTY_JOIN_DIRECT_BUFFER_BO;
        }

        List<JoinLongFieldBo> directCountFieldBoList = joinDirectBufferBoList.stream().map(JoinDirectBufferBo::getDirectCountJoinValue).collect(Collectors.toList());
        JoinLongFieldBo directCountJoinValue = JoinLongFieldBo.merge(directCountFieldBoList);

        List<JoinLongFieldBo> directMemoryUsedFieldBoList = joinDirectBufferBoList.stream().map(JoinDirectBufferBo::getDirectMemoryUsedJoinValue).collect(Collectors.toList());
        JoinLongFieldBo directMemoryUsedJoinValue = JoinLongFieldBo.merge(directMemoryUsedFieldBoList);

        List<JoinLongFieldBo> mappedCountFieldBoList = joinDirectBufferBoList.stream().map(JoinDirectBufferBo::getMappedCountJoinValue).collect(Collectors.toList());
        JoinLongFieldBo mappedCountJoinValue = JoinLongFieldBo.merge(mappedCountFieldBoList);

        List<JoinLongFieldBo> mappedMemoryUsedFieldBoList = joinDirectBufferBoList.stream().map(JoinDirectBufferBo::getMappedMemoryUsedJoinValue).collect(Collectors.toList());
        JoinLongFieldBo mappedMemoryUsedJoinValue = JoinLongFieldBo.merge(mappedMemoryUsedFieldBoList);

        JoinDirectBufferBo firstJoinDirectBufferBo = joinDirectBufferBoList.get(0);

        JoinDirectBufferBo newJoinDirectBufferBo = new JoinDirectBufferBo();
        newJoinDirectBufferBo.setId(firstJoinDirectBufferBo.getId());
        newJoinDirectBufferBo.setTimestamp(timestamp);
        newJoinDirectBufferBo.setDirectCountJoinValue(directCountJoinValue);
        newJoinDirectBufferBo.setDirectMemoryUsedJoinValue(directMemoryUsedJoinValue);
        newJoinDirectBufferBo.setMappedCountJoinValue(mappedCountJoinValue);
        newJoinDirectBufferBo.setMappedMemoryUsedJoinValue(mappedMemoryUsedJoinValue);
        return newJoinDirectBufferBo;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public JoinLongFieldBo getDirectCountJoinValue() {
        return directCountJoinValue;
    }

    public void setDirectCountJoinValue(JoinLongFieldBo directCountJoinValue) {
        this.directCountJoinValue = directCountJoinValue;
    }

    public JoinLongFieldBo getDirectMemoryUsedJoinValue() {
        return directMemoryUsedJoinValue;
    }

    public void setDirectMemoryUsedJoinValue(JoinLongFieldBo directMemoryUsedJoinValue) {
        this.directMemoryUsedJoinValue = directMemoryUsedJoinValue;
    }

    public JoinLongFieldBo getMappedCountJoinValue() {
        return mappedCountJoinValue;
    }

    public void setMappedCountJoinValue(JoinLongFieldBo mappedCountJoinValue) {
        this.mappedCountJoinValue = mappedCountJoinValue;
    }

    public JoinLongFieldBo getMappedMemoryUsedJoinValue() {
        return mappedMemoryUsedJoinValue;
    }

    public void setMappedMemoryUsedJoinValue(JoinLongFieldBo mappedMemoryUsedJoinValue) {
        this.mappedMemoryUsedJoinValue = mappedMemoryUsedJoinValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinDirectBufferBo that = (JoinDirectBufferBo) o;

        if (timestamp != that.timestamp) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (directCountJoinValue != null ? !directCountJoinValue.equals(that.directCountJoinValue) : that.directCountJoinValue != null) return false;
        if (directMemoryUsedJoinValue != null ? !directMemoryUsedJoinValue.equals(that.directMemoryUsedJoinValue) : that.directMemoryUsedJoinValue != null) return false;
        if (mappedCountJoinValue != null ? !mappedCountJoinValue.equals(that.mappedCountJoinValue) : that.mappedCountJoinValue != null) return false;
        return mappedMemoryUsedJoinValue != null ? mappedMemoryUsedJoinValue.equals(that.mappedMemoryUsedJoinValue) : that.mappedMemoryUsedJoinValue == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (directCountJoinValue != null ? directCountJoinValue.hashCode() : 0);
        result = 31 * result + (directMemoryUsedJoinValue != null ? directMemoryUsedJoinValue.hashCode() : 0);
        result = 31 * result + (mappedCountJoinValue != null ? mappedCountJoinValue.hashCode() : 0);
        result = 31 * result + (mappedMemoryUsedJoinValue != null ? mappedMemoryUsedJoinValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JoinDirectBufferBo{");
        sb.append("id='").append(id).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", directCountJoinValue=").append(directCountJoinValue);
        sb.append(", directMemoryUsedJoinValue=").append(directMemoryUsedJoinValue);
        sb.append(", mappedCountJoinValue=").append(mappedCountJoinValue);
        sb.append(", mappedMemoryUsedJoinValue=").append(mappedMemoryUsedJoinValue);
        sb.append('}');
        return sb.toString();
    }

}
