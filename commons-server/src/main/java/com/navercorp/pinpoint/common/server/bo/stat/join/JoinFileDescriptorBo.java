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

/**
 * @author Roy Kim
 */
public class JoinFileDescriptorBo implements JoinStatBo {
    public static final JoinFileDescriptorBo EMPTY_JOIN_FILE_DESCRIPTOR_BO = new JoinFileDescriptorBo();
    public static final long UNCOLLECTED_VALUE = -1;

    private String id = UNKNOWN_ID;
    private long timestamp = Long.MIN_VALUE;

    private long avgOpenFDCount = UNCOLLECTED_VALUE;
    private String maxOpenFDCountAgentId = UNKNOWN_AGENT;
    private long maxOpenFDCount = UNCOLLECTED_VALUE;
    private String minOpenFDCountAgentId = UNKNOWN_AGENT;
    private long minOpenFDCount = UNCOLLECTED_VALUE;

    public JoinFileDescriptorBo() {
    }

    public JoinFileDescriptorBo(String id, long avgOpenFDCount, long maxOpenFDCount, String maxOpenFDCountAgentId, long minOpenFDCount, String minOpenFDCountAgentId, long timestamp) {
        this.id = id;
        this.avgOpenFDCount = avgOpenFDCount;
        this.minOpenFDCount = minOpenFDCount;
        this.minOpenFDCountAgentId = minOpenFDCountAgentId;
        this.maxOpenFDCount = maxOpenFDCount;
        this.maxOpenFDCountAgentId = maxOpenFDCountAgentId;
        this.timestamp = timestamp;
    }

    public static JoinFileDescriptorBo joinFileDescriptorBoList(List<JoinFileDescriptorBo> joinFileDescriptorBoList, Long timestamp) {
        int boCount = joinFileDescriptorBoList.size();

        if (joinFileDescriptorBoList.size() == 0) {
            return EMPTY_JOIN_FILE_DESCRIPTOR_BO;
        }

        JoinFileDescriptorBo newJoinFileDescriptorBo = new JoinFileDescriptorBo();
        JoinFileDescriptorBo initJoinFileDescriptorBo = joinFileDescriptorBoList.get(0);
        newJoinFileDescriptorBo.setId(initJoinFileDescriptorBo.getId());
        newJoinFileDescriptorBo.setTimestamp(timestamp);

        long sumCount = 0L;
        String maxOpenFDCountAgentId = initJoinFileDescriptorBo.getMaxOpenFDCountAgentId();
        long maxOpenFDCount = initJoinFileDescriptorBo.getMaxOpenFDCount();
        String minOpenFDCountAgentId = initJoinFileDescriptorBo.getMinOpenFDCountAgentId();
        long minOpenFDCount = initJoinFileDescriptorBo.getMinOpenFDCount();

        for (JoinFileDescriptorBo joinFileDescriptorBo : joinFileDescriptorBoList) {
            sumCount += joinFileDescriptorBo.getAvgOpenFDCount();
            if (joinFileDescriptorBo.getMaxOpenFDCount() > maxOpenFDCount) {
                maxOpenFDCount = joinFileDescriptorBo.getMaxOpenFDCount();
                maxOpenFDCountAgentId = joinFileDescriptorBo.getMaxOpenFDCountAgentId();
            }
            if (joinFileDescriptorBo.getMinOpenFDCount() < minOpenFDCount) {
                minOpenFDCount = joinFileDescriptorBo.getMinOpenFDCount();
                minOpenFDCountAgentId = joinFileDescriptorBo.getMinOpenFDCountAgentId();
            }
        }

        newJoinFileDescriptorBo.setAvgOpenFDCount((sumCount / boCount));
        newJoinFileDescriptorBo.setMaxOpenFDCount(maxOpenFDCount);
        newJoinFileDescriptorBo.setMaxOpenFDCountAgentId(maxOpenFDCountAgentId);
        newJoinFileDescriptorBo.setMinOpenFDCount(minOpenFDCount);
        newJoinFileDescriptorBo.setMinOpenFDCountAgentId(minOpenFDCountAgentId);

        return newJoinFileDescriptorBo;
    }

    public String getMaxOpenFDCountAgentId() {
        return maxOpenFDCountAgentId;
    }

    public String getMinOpenFDCountAgentId() {
        return minOpenFDCountAgentId;
    }

    public void setMaxOpenFDCountAgentId(String maxOpenFDCountAgentId) {
        this.maxOpenFDCountAgentId = maxOpenFDCountAgentId;
    }

    public void setMinOpenFDCountAgentId(String minOpenFDCountAgentId) {
        this.minOpenFDCountAgentId = minOpenFDCountAgentId;
    }

    public void setAvgOpenFDCount(long avgOpenFDCount) {
        this.avgOpenFDCount = avgOpenFDCount;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getAvgOpenFDCount() {
        return avgOpenFDCount;
    }

    public String getId() {
        return id;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setMaxOpenFDCount(long maxOpenFDCount) {
        this.maxOpenFDCount = maxOpenFDCount;
    }

    public void setMinOpenFDCount(long minOpenFDCount) {
        this.minOpenFDCount = minOpenFDCount;
    }

    public long getMaxOpenFDCount() {
        return maxOpenFDCount;
    }

    public long getMinOpenFDCount() {
        return minOpenFDCount;
    }

    @Override
    public String toString() {
        return "JoinFileDescriptorBo{" +
            "id='" + id + '\'' +
            ", avgOpenFDCount=" + avgOpenFDCount +
            ", maxOpenFDCountAgentId='" + maxOpenFDCountAgentId + '\'' +
            ", maxOpenFDCount=" + maxOpenFDCount +
            ", minOpenFDCountAgentId='" + minOpenFDCountAgentId + '\'' +
            ", minOpenFDCount=" + minOpenFDCount +
            ", timestamp=" + timestamp +"(" + new Date(timestamp)+ ")" +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinFileDescriptorBo that = (JoinFileDescriptorBo) o;

        if (timestamp != that.timestamp) return false;
        if (avgOpenFDCount != that.avgOpenFDCount) return false;
        if (maxOpenFDCount != that.maxOpenFDCount) return false;
        if (minOpenFDCount != that.minOpenFDCount) return false;
        if (!id.equals(that.id)) return false;
        if (!maxOpenFDCountAgentId.equals(that.maxOpenFDCountAgentId)) return false;
        return minOpenFDCountAgentId.equals(that.minOpenFDCountAgentId);

    }

    @Override
    public int hashCode() {
        int result;

        result = id.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) (avgOpenFDCount ^ (avgOpenFDCount >>> 32));
        result = 31 * result + maxOpenFDCountAgentId.hashCode();
        result = 31 * result + (int) (maxOpenFDCount ^ (maxOpenFDCount >>> 32));
        result = 31 * result + minOpenFDCountAgentId.hashCode();
        result = 31 * result + (int) (minOpenFDCount ^ (minOpenFDCount >>> 32));

        return result;
    }
}
