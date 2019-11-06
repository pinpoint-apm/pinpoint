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

import java.util.Date;
import java.util.List;

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
    private int totalCount = UNCOLLECTED_VALUE;
    private String maxTotalCountAgentId = UNKNOWN_AGENT;
    private int maxTotalCount = UNCOLLECTED_VALUE;
    private String minTotalCountAgentId = UNKNOWN_AGENT;
    private int minTotalCount = UNCOLLECTED_VALUE;

    public JoinActiveTraceBo() {
    }

    public JoinActiveTraceBo(String id, int histogramSchemaType, short version, int totalCount, int minTotalCount, String minTotalCountAgentId, int maxTotalCount, String maxTotalCountAgentId, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
        this.histogramSchemaType = histogramSchemaType;
        this.version = version;
        this.totalCount = totalCount;
        this.maxTotalCountAgentId = maxTotalCountAgentId;
        this.maxTotalCount = maxTotalCount;
        this.minTotalCountAgentId = minTotalCountAgentId;
        this.minTotalCount = minTotalCount;
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

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getMaxTotalCountAgentId() {
        return maxTotalCountAgentId;
    }

    public void setMaxTotalCountAgentId(String maxTotalCountAgentId) {
        this.maxTotalCountAgentId = maxTotalCountAgentId;
    }

    public int getMaxTotalCount() {
        return maxTotalCount;
    }

    public void setMaxTotalCount(int maxTotalCount) {
        this.maxTotalCount = maxTotalCount;
    }

    public String getMinTotalCountAgentId() {
        return minTotalCountAgentId;
    }

    public void setMinTotalCountAgentId(String minTotalCountAgentId) {
        this.minTotalCountAgentId = minTotalCountAgentId;
    }

    public int getMinTotalCount() {
        return minTotalCount;
    }

    public void setMinTotalCount(int minTotalCount) {
        this.minTotalCount = minTotalCount;
    }

    public static JoinActiveTraceBo joinActiveTraceBoList(List<JoinActiveTraceBo> joinActiveTraceBoList, Long timestamp) {
        final int boCount = joinActiveTraceBoList.size();

        if (boCount == 0) {
            return JoinActiveTraceBo.EMPTY_JOIN_ACTIVE_TRACE_BO;
        }

        final JoinActiveTraceBo initJoinActiveTraceBo = joinActiveTraceBoList.get(0);
        int sumTotalcount = 0;
        String maxTotalCountAgentId = initJoinActiveTraceBo.getMaxTotalCountAgentId();
        int maxTotalCount = initJoinActiveTraceBo.getMaxTotalCount();
        String minTotalCountAgentId = initJoinActiveTraceBo.getMinTotalCountAgentId();
        int minTotalCount = initJoinActiveTraceBo.getMinTotalCount();

        for (JoinActiveTraceBo joinActiveTraceBo : joinActiveTraceBoList) {
            sumTotalcount += joinActiveTraceBo.getTotalCount();

            if (joinActiveTraceBo.getMaxTotalCount() > maxTotalCount) {
                maxTotalCount = joinActiveTraceBo.getMaxTotalCount();
                maxTotalCountAgentId = joinActiveTraceBo.getMaxTotalCountAgentId();
            }
            if (joinActiveTraceBo.getMinTotalCount() < minTotalCount) {
                minTotalCount = joinActiveTraceBo.getMinTotalCount();
                minTotalCountAgentId = joinActiveTraceBo.getMinTotalCountAgentId();
            }
        }

        final JoinActiveTraceBo newJoinActiveTraceBo = new JoinActiveTraceBo();
        newJoinActiveTraceBo.setId(initJoinActiveTraceBo.getId());
        newJoinActiveTraceBo.setTimestamp(timestamp);
        newJoinActiveTraceBo.setHistogramSchemaType(initJoinActiveTraceBo.getHistogramSchemaType());
        newJoinActiveTraceBo.setVersion(initJoinActiveTraceBo.getVersion());
        newJoinActiveTraceBo.setTotalCount(sumTotalcount / boCount);
        newJoinActiveTraceBo.setMaxTotalCount(maxTotalCount);
        newJoinActiveTraceBo.setMaxTotalCountAgentId(maxTotalCountAgentId);
        newJoinActiveTraceBo.setMinTotalCount(minTotalCount);
        newJoinActiveTraceBo.setMinTotalCountAgentId(minTotalCountAgentId);

        return newJoinActiveTraceBo;
    }

    @Override
    public String toString() {
        return "JoinActiveTraceBo{" +
            "id='" + id + '\'' +
            ", timestamp=" + new Date(timestamp) +
            ", histogramSchemaType=" + histogramSchemaType +
            ", version=" + version +
            ", totalCount=" + totalCount +
            ", maxTotalCountAgentId='" + maxTotalCountAgentId + '\'' +
            ", maxTotalCount=" + maxTotalCount +
            ", minTotalCountAgentId='" + minTotalCountAgentId + '\'' +
            ", minTotalCount=" + minTotalCount +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinActiveTraceBo that = (JoinActiveTraceBo) o;

        if (timestamp != that.timestamp) return false;
        if (histogramSchemaType != that.histogramSchemaType) return false;
        if (version != that.version) return false;
        if (totalCount != that.totalCount) return false;
        if (maxTotalCount != that.maxTotalCount) return false;
        if (minTotalCount != that.minTotalCount) return false;
        if (!id.equals(that.id)) return false;
        if (!maxTotalCountAgentId.equals(that.maxTotalCountAgentId)) return false;
        return minTotalCountAgentId.equals(that.minTotalCountAgentId);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + histogramSchemaType;
        result = 31 * result + (int) version;
        result = 31 * result + totalCount;
        result = 31 * result + maxTotalCountAgentId.hashCode();
        result = 31 * result + maxTotalCount;
        result = 31 * result + minTotalCountAgentId.hashCode();
        result = 31 * result + minTotalCount;
        return result;
    }
}
