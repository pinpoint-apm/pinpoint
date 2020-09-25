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
import java.util.stream.Collectors;

/**
 * @author minwoo.jung
 */
public class JoinActiveTraceBo implements JoinStatBo {

    public static final JoinActiveTraceBo EMPTY_JOIN_ACTIVE_TRACE_BO = new JoinActiveTraceBo();
    public static final int UNCOLLECTED_VALUE = -1;
    private static final int DEFAULT_HISTOGRAM_SCHEMA_TYPE = -1;
    private static final short DEFAULT_VERSION = -1;

    private String id = UNKNOWN_ID;
    private long timestamp = Long.MIN_VALUE;
    private int histogramSchemaType = DEFAULT_HISTOGRAM_SCHEMA_TYPE;
    private short version = DEFAULT_VERSION;
    private JoinIntFieldBo totalCountJoinValue = JoinIntFieldBo.UNCOLLECTED_FIELD_BO;

    public JoinActiveTraceBo() {
    }

    public JoinActiveTraceBo(String id, int histogramSchemaType, short version, int totalCount, int minTotalCount, String minTotalCountAgentId, int maxTotalCount, String maxTotalCountAgentId, long timestamp) {
        this(id, histogramSchemaType, version, new JoinIntFieldBo(totalCount, minTotalCount, minTotalCountAgentId, maxTotalCount, maxTotalCountAgentId), timestamp);
    }

    public JoinActiveTraceBo(String id, int histogramSchemaType, short version, JoinIntFieldBo totalCountJoinValue, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
        this.histogramSchemaType = histogramSchemaType;
        this.version = version;
        this.totalCountJoinValue = totalCountJoinValue;
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

    public int getHistogramSchemaType() {
        return histogramSchemaType;
    }

    public void setHistogramSchemaType(int histogramSchemaType) {
        this.histogramSchemaType = histogramSchemaType;
    }

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public JoinIntFieldBo getTotalCountJoinValue() {
        return totalCountJoinValue;
    }

    public void setTotalCountJoinValue(JoinIntFieldBo totalCountJoinValue) {
        this.totalCountJoinValue = totalCountJoinValue;
    }

    public static void apply(JoinApplicationStatBo.Builder builder, List<JoinActiveTraceBo> joinActiveTraceBoList, Long timestamp) {
        builder.addActiveTrace(joinActiveTraceBoList(joinActiveTraceBoList, timestamp));
    }

    public static JoinActiveTraceBo joinActiveTraceBoList(List<JoinActiveTraceBo> joinActiveTraceBoList, Long timestamp) {
        if (joinActiveTraceBoList.isEmpty()) {
            return JoinActiveTraceBo.EMPTY_JOIN_ACTIVE_TRACE_BO;
        }

        List<JoinIntFieldBo> totalCountFieldBoList = joinActiveTraceBoList.stream().map(JoinActiveTraceBo::getTotalCountJoinValue).collect(Collectors.toList());
        JoinIntFieldBo totalCountJoinValue = JoinIntFieldBo.merge(totalCountFieldBoList);

        final JoinActiveTraceBo firstJoinActiveTraceBo = joinActiveTraceBoList.get(0);

        final JoinActiveTraceBo newJoinActiveTraceBo = new JoinActiveTraceBo();
        newJoinActiveTraceBo.setId(firstJoinActiveTraceBo.getId());
        newJoinActiveTraceBo.setTimestamp(timestamp);
        newJoinActiveTraceBo.setHistogramSchemaType(firstJoinActiveTraceBo.getHistogramSchemaType());
        newJoinActiveTraceBo.setVersion(firstJoinActiveTraceBo.getVersion());
        newJoinActiveTraceBo.setTotalCountJoinValue(totalCountJoinValue);
        return newJoinActiveTraceBo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JoinActiveTraceBo{");
        sb.append("id='").append(id).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", histogramSchemaType=").append(histogramSchemaType);
        sb.append(", version=").append(version);
        sb.append(", totalCountJoinValue=").append(totalCountJoinValue);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinActiveTraceBo that = (JoinActiveTraceBo) o;

        if (timestamp != that.timestamp) return false;
        if (histogramSchemaType != that.histogramSchemaType) return false;
        if (version != that.version) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return totalCountJoinValue != null ? totalCountJoinValue.equals(that.totalCountJoinValue) : that.totalCountJoinValue == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + histogramSchemaType;
        result = 31 * result + (int) version;
        result = 31 * result + (totalCountJoinValue != null ? totalCountJoinValue.hashCode() : 0);
        return result;
    }
}
