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
public class JoinDirectBufferBo implements JoinStatBo {
    public static final JoinDirectBufferBo EMPTY_JOIN_DIRECT_BUFFER_BO = new JoinDirectBufferBo();
    public static final long UNCOLLECTED_VALUE = -1;

    private String id = UNKNOWN_ID;
    private long timestamp = Long.MIN_VALUE;

    private long avgDirectCount = UNCOLLECTED_VALUE;
    private String maxDirectCountAgentId = UNKNOWN_AGENT;
    private long maxDirectCount = UNCOLLECTED_VALUE;
    private String minDirectCountAgentId = UNKNOWN_AGENT;
    private long minDirectCount = UNCOLLECTED_VALUE;

    private long avgDirectMemoryUsed = UNCOLLECTED_VALUE;
    private String maxDirectMemoryUsedAgentId = UNKNOWN_AGENT;
    private long maxDirectMemoryUsed = UNCOLLECTED_VALUE;
    private String minDirectMemoryUsedAgentId = UNKNOWN_AGENT;
    private long minDirectMemoryUsed = UNCOLLECTED_VALUE;

    private long avgMappedCount = UNCOLLECTED_VALUE;
    private String maxMappedCountAgentId = UNKNOWN_AGENT;
    private long maxMappedCount = UNCOLLECTED_VALUE;
    private String minMappedCountAgentId = UNKNOWN_AGENT;
    private long minMappedCount = UNCOLLECTED_VALUE;

    private long avgMappedMemoryUsed = UNCOLLECTED_VALUE;
    private String maxMappedMemoryUsedAgentId = UNKNOWN_AGENT;
    private long maxMappedMemoryUsed = UNCOLLECTED_VALUE;
    private String minMappedMemoryUsedAgentId = UNKNOWN_AGENT;
    private long minMappedMemoryUsed = UNCOLLECTED_VALUE;

    public JoinDirectBufferBo() {
    }

    public JoinDirectBufferBo(String id, long avgDirectCount, long maxDirectCount, String maxDirectCountAgentId, long minDirectCount, String minDirectCountAgentId
            , long avgDirectMemoryUsed, long maxDirectMemoryUsed, String maxDirectMemoryUsedAgentId, long minDirectMemoryUsed, String minDirectMemoryUsedAgentId
            , long avgMappedCount, long maxMappedCount, String maxMappedCountAgentId, long minMappedCount, String minMappedCountAgentId
            , long avgMappedMemoryUsed, long maxMappedMemoryUsed, String maxMappedMemoryUsedAgentId, long minMappedMemoryUsed, String minMappedMemoryUsedAgentId
            , long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
        this.avgDirectCount = avgDirectCount;
        this.maxDirectCountAgentId = maxDirectCountAgentId;
        this.maxDirectCount = maxDirectCount;
        this.minDirectCountAgentId = minDirectCountAgentId;
        this.minDirectCount = minDirectCount;
        this.avgDirectMemoryUsed = avgDirectMemoryUsed;
        this.maxDirectMemoryUsedAgentId = maxDirectMemoryUsedAgentId;
        this.maxDirectMemoryUsed = maxDirectMemoryUsed;
        this.minDirectMemoryUsedAgentId = minDirectMemoryUsedAgentId;
        this.minDirectMemoryUsed = minDirectMemoryUsed;
        this.avgMappedCount = avgMappedCount;
        this.maxMappedCountAgentId = maxMappedCountAgentId;
        this.maxMappedCount = maxMappedCount;
        this.minMappedCountAgentId = minMappedCountAgentId;
        this.minMappedCount = minMappedCount;
        this.avgMappedMemoryUsed = avgMappedMemoryUsed;
        this.maxMappedMemoryUsedAgentId = maxMappedMemoryUsedAgentId;
        this.maxMappedMemoryUsed = maxMappedMemoryUsed;
        this.minMappedMemoryUsedAgentId = minMappedMemoryUsedAgentId;
        this.minMappedMemoryUsed = minMappedMemoryUsed;
    }

    public static JoinDirectBufferBo joinDirectBufferBoList(List<JoinDirectBufferBo> joinDirectBufferBoList, Long timestamp) {
        int boCount = joinDirectBufferBoList.size();

        if (joinDirectBufferBoList.size() == 0) {
            return EMPTY_JOIN_DIRECT_BUFFER_BO;
        }

        JoinDirectBufferBo newJoinDirectBufferBo = new JoinDirectBufferBo();
        JoinDirectBufferBo initJoinDirectBufferBo = joinDirectBufferBoList.get(0);
        newJoinDirectBufferBo.setId(initJoinDirectBufferBo.getId());
        newJoinDirectBufferBo.setTimestamp(timestamp);

        long sumDirectCount = 0L;
        String maxDirectCountAgentId = initJoinDirectBufferBo.getMaxDirectCountAgentId();
        long maxDirectCount = initJoinDirectBufferBo.getMaxDirectCount();
        String minDirectCountAgentId = initJoinDirectBufferBo.getMinDirectCountAgentId();
        long minDirectCount = initJoinDirectBufferBo.getMinDirectCount();

        long sumDirectMemoryUsed = 0L;
        String maxDirectMemoryUsedAgentId = initJoinDirectBufferBo.getMaxDirectMemoryUsedAgentId();
        long maxDirectMemoryUsed = initJoinDirectBufferBo.getMaxDirectMemoryUsed();
        String minDirectMemoryUsedAgentId = initJoinDirectBufferBo.getMinDirectMemoryUsedAgentId();
        long minDirectMemoryUsed = initJoinDirectBufferBo.getMinDirectMemoryUsed();

        long sumMappedCount = 0L;
        String maxMappedCountAgentId = initJoinDirectBufferBo.getMaxMappedCountAgentId();
        long maxMappedCount = initJoinDirectBufferBo.getMaxMappedCount();
        String minMappedCountAgentId = initJoinDirectBufferBo.getMinMappedCountAgentId();
        long minMappedCount = initJoinDirectBufferBo.getMinMappedCount();

        long sumMappedMemoryUsed = 0L;
        String maxMappedMemoryUsedAgentId = initJoinDirectBufferBo.getMaxMappedMemoryUsedAgentId();
        long maxMappedMemoryUsed = initJoinDirectBufferBo.getMaxMappedMemoryUsed();
        String minMappedMemoryUsedAgentId = initJoinDirectBufferBo.getMinMappedMemoryUsedAgentId();
        long minMappedMemoryUsed = initJoinDirectBufferBo.getMinMappedMemoryUsed();

        for (JoinDirectBufferBo joinDirectBufferBo : joinDirectBufferBoList) {

            sumDirectCount += joinDirectBufferBo.getAvgDirectCount();
            if (joinDirectBufferBo.getMaxDirectCount() > maxDirectCount) {
                maxDirectCount = joinDirectBufferBo.getMaxDirectCount();
                maxDirectCountAgentId = joinDirectBufferBo.getMaxDirectCountAgentId();
            }
            if (joinDirectBufferBo.getMinDirectCount() < minDirectCount) {
                minDirectCount = joinDirectBufferBo.getMinDirectCount();
                minDirectCountAgentId = joinDirectBufferBo.getMinDirectCountAgentId();
            }

            sumDirectMemoryUsed += joinDirectBufferBo.getAvgDirectMemoryUsed();
            if (joinDirectBufferBo.getMaxDirectMemoryUsed() > maxDirectMemoryUsed) {
                maxDirectMemoryUsed = joinDirectBufferBo.getMaxDirectMemoryUsed();
                maxDirectMemoryUsedAgentId = joinDirectBufferBo.getMaxDirectMemoryUsedAgentId();
            }
            if (joinDirectBufferBo.getMinDirectMemoryUsed() < minDirectMemoryUsed) {
                minDirectMemoryUsed = joinDirectBufferBo.getMinDirectMemoryUsed();
                minDirectMemoryUsedAgentId = joinDirectBufferBo.getMinDirectMemoryUsedAgentId();
            }

            sumMappedCount += joinDirectBufferBo.getAvgMappedCount();
            if (joinDirectBufferBo.getMaxMappedCount() > maxMappedCount) {
                maxMappedCount = joinDirectBufferBo.getMaxMappedCount();
                maxMappedCountAgentId = joinDirectBufferBo.getMaxMappedCountAgentId();
            }
            if (joinDirectBufferBo.getMinMappedCount() < minMappedCount) {
                minMappedCount = joinDirectBufferBo.getMinMappedCount();
                minMappedCountAgentId = joinDirectBufferBo.getMinMappedCountAgentId();
            }

            sumMappedMemoryUsed += joinDirectBufferBo.getAvgMappedMemoryUsed();
            if (joinDirectBufferBo.getMaxMappedMemoryUsed() > maxMappedMemoryUsed) {
                maxMappedMemoryUsed = joinDirectBufferBo.getMaxMappedMemoryUsed();
                maxMappedMemoryUsedAgentId = joinDirectBufferBo.getMaxMappedMemoryUsedAgentId();
            }
            if (joinDirectBufferBo.getMinMappedMemoryUsed() < minMappedMemoryUsed) {
                minMappedMemoryUsed = joinDirectBufferBo.getMinMappedMemoryUsed();
                minMappedMemoryUsedAgentId = joinDirectBufferBo.getMinMappedMemoryUsedAgentId();
            }
        }

        newJoinDirectBufferBo.setAvgDirectCount((sumDirectCount / boCount));
        newJoinDirectBufferBo.setMaxDirectCount(maxDirectCount);
        newJoinDirectBufferBo.setMaxDirectCountAgentId(maxDirectCountAgentId);
        newJoinDirectBufferBo.setMinDirectCount(minDirectCount);
        newJoinDirectBufferBo.setMinDirectCountAgentId(minDirectCountAgentId);

        newJoinDirectBufferBo.setAvgDirectMemoryUsed((sumDirectMemoryUsed / boCount));
        newJoinDirectBufferBo.setMaxDirectMemoryUsed(maxDirectMemoryUsed);
        newJoinDirectBufferBo.setMaxDirectMemoryUsedAgentId(maxDirectMemoryUsedAgentId);
        newJoinDirectBufferBo.setMinDirectMemoryUsed(minDirectMemoryUsed);
        newJoinDirectBufferBo.setMinDirectMemoryUsedAgentId(minDirectMemoryUsedAgentId);

        newJoinDirectBufferBo.setAvgMappedCount((sumMappedCount / boCount));
        newJoinDirectBufferBo.setMaxMappedCount(maxMappedCount);
        newJoinDirectBufferBo.setMaxMappedCountAgentId(maxMappedCountAgentId);
        newJoinDirectBufferBo.setMinMappedCount(minMappedCount);
        newJoinDirectBufferBo.setMinMappedCountAgentId(minMappedCountAgentId);

        newJoinDirectBufferBo.setAvgMappedMemoryUsed((sumMappedMemoryUsed / boCount));
        newJoinDirectBufferBo.setMaxMappedMemoryUsed(maxMappedMemoryUsed);
        newJoinDirectBufferBo.setMaxMappedMemoryUsedAgentId(maxMappedMemoryUsedAgentId);
        newJoinDirectBufferBo.setMinMappedMemoryUsed(minMappedMemoryUsed);
        newJoinDirectBufferBo.setMinMappedMemoryUsedAgentId(minMappedMemoryUsedAgentId);

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

    public long getAvgDirectCount() {
        return avgDirectCount;
    }

    public void setAvgDirectCount(long avgDirectCount) {
        this.avgDirectCount = avgDirectCount;
    }

    public String getMaxDirectCountAgentId() {
        return maxDirectCountAgentId;
    }

    public void setMaxDirectCountAgentId(String maxDirectCountAgentId) {
        this.maxDirectCountAgentId = maxDirectCountAgentId;
    }

    public long getMaxDirectCount() {
        return maxDirectCount;
    }

    public void setMaxDirectCount(long maxDirectCount) {
        this.maxDirectCount = maxDirectCount;
    }

    public String getMinDirectCountAgentId() {
        return minDirectCountAgentId;
    }

    public void setMinDirectCountAgentId(String minDirectCountAgentId) {
        this.minDirectCountAgentId = minDirectCountAgentId;
    }

    public long getMinDirectCount() {
        return minDirectCount;
    }

    public void setMinDirectCount(long minDirectCount) {
        this.minDirectCount = minDirectCount;
    }

    public long getAvgDirectMemoryUsed() {
        return avgDirectMemoryUsed;
    }

    public void setAvgDirectMemoryUsed(long avgDirectMemoryUsed) {
        this.avgDirectMemoryUsed = avgDirectMemoryUsed;
    }

    public String getMaxDirectMemoryUsedAgentId() {
        return maxDirectMemoryUsedAgentId;
    }

    public void setMaxDirectMemoryUsedAgentId(String maxDirectMemoryUsedAgentId) {
        this.maxDirectMemoryUsedAgentId = maxDirectMemoryUsedAgentId;
    }

    public long getMaxDirectMemoryUsed() {
        return maxDirectMemoryUsed;
    }

    public void setMaxDirectMemoryUsed(long maxDirectMemoryUsed) {
        this.maxDirectMemoryUsed = maxDirectMemoryUsed;
    }

    public String getMinDirectMemoryUsedAgentId() {
        return minDirectMemoryUsedAgentId;
    }

    public void setMinDirectMemoryUsedAgentId(String minDirectMemoryUsedAgentId) {
        this.minDirectMemoryUsedAgentId = minDirectMemoryUsedAgentId;
    }

    public long getMinDirectMemoryUsed() {
        return minDirectMemoryUsed;
    }

    public void setMinDirectMemoryUsed(long minDirectMemoryUsed) {
        this.minDirectMemoryUsed = minDirectMemoryUsed;
    }

    public long getAvgMappedCount() {
        return avgMappedCount;
    }

    public void setAvgMappedCount(long avgMappedCount) {
        this.avgMappedCount = avgMappedCount;
    }

    public String getMaxMappedCountAgentId() {
        return maxMappedCountAgentId;
    }

    public void setMaxMappedCountAgentId(String maxMappedCountAgentId) {
        this.maxMappedCountAgentId = maxMappedCountAgentId;
    }

    public long getMaxMappedCount() {
        return maxMappedCount;
    }

    public void setMaxMappedCount(long maxMappedCount) {
        this.maxMappedCount = maxMappedCount;
    }

    public String getMinMappedCountAgentId() {
        return minMappedCountAgentId;
    }

    public void setMinMappedCountAgentId(String minMappedCountAgentId) {
        this.minMappedCountAgentId = minMappedCountAgentId;
    }

    public long getMinMappedCount() {
        return minMappedCount;
    }

    public void setMinMappedCount(long minMappedCount) {
        this.minMappedCount = minMappedCount;
    }

    public long getAvgMappedMemoryUsed() {
        return avgMappedMemoryUsed;
    }

    public void setAvgMappedMemoryUsed(long avgMappedMemoryUsed) {
        this.avgMappedMemoryUsed = avgMappedMemoryUsed;
    }

    public String getMaxMappedMemoryUsedAgentId() {
        return maxMappedMemoryUsedAgentId;
    }

    public void setMaxMappedMemoryUsedAgentId(String maxMappedMemoryUsedAgentId) {
        this.maxMappedMemoryUsedAgentId = maxMappedMemoryUsedAgentId;
    }

    public long getMaxMappedMemoryUsed() {
        return maxMappedMemoryUsed;
    }

    public void setMaxMappedMemoryUsed(long maxMappedMemoryUsed) {
        this.maxMappedMemoryUsed = maxMappedMemoryUsed;
    }

    public String getMinMappedMemoryUsedAgentId() {
        return minMappedMemoryUsedAgentId;
    }

    public void setMinMappedMemoryUsedAgentId(String minMappedMemoryUsedAgentId) {
        this.minMappedMemoryUsedAgentId = minMappedMemoryUsedAgentId;
    }

    public long getMinMappedMemoryUsed() {
        return minMappedMemoryUsed;
    }

    public void setMinMappedMemoryUsed(long minMappedMemoryUsed) {
        this.minMappedMemoryUsed = minMappedMemoryUsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinDirectBufferBo that = (JoinDirectBufferBo) o;

        if (timestamp != that.timestamp) return false;
        if (avgDirectCount != that.avgDirectCount) return false;
        if (maxDirectCount != that.maxDirectCount) return false;
        if (minDirectCount != that.minDirectCount) return false;
        if (avgDirectMemoryUsed != that.avgDirectMemoryUsed) return false;
        if (maxDirectMemoryUsed != that.maxDirectMemoryUsed) return false;
        if (minDirectMemoryUsed != that.minDirectMemoryUsed) return false;
        if (avgMappedCount != that.avgMappedCount) return false;
        if (maxMappedCount != that.maxMappedCount) return false;
        if (minMappedCount != that.minMappedCount) return false;
        if (avgMappedMemoryUsed != that.avgMappedMemoryUsed) return false;
        if (maxMappedMemoryUsed != that.maxMappedMemoryUsed) return false;
        if (minMappedMemoryUsed != that.minMappedMemoryUsed) return false;
        if (!id.equals(that.id)) return false;
        if (!maxDirectCountAgentId.equals(that.maxDirectCountAgentId)) return false;
        if (!minDirectCountAgentId.equals(that.minDirectCountAgentId)) return false;
        if (!maxDirectMemoryUsedAgentId.equals(that.maxDirectMemoryUsedAgentId)) return false;
        if (!minDirectMemoryUsedAgentId.equals(that.minDirectMemoryUsedAgentId)) return false;
        if (!maxMappedCountAgentId.equals(that.maxMappedCountAgentId)) return false;
        if (!minMappedCountAgentId.equals(that.minMappedCountAgentId)) return false;
        if (!maxMappedMemoryUsedAgentId.equals(that.maxMappedMemoryUsedAgentId)) return false;
        return minMappedMemoryUsedAgentId.equals(that.minMappedMemoryUsedAgentId);

    }

    @Override
    public int hashCode() {
        int result;

        result = id.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));

        result = 31 * result + (int) (avgDirectCount ^ (avgDirectCount >>> 32));
        result = 31 * result + maxDirectCountAgentId.hashCode();
        result = 31 * result + (int) (maxDirectCount ^ (maxDirectCount >>> 32));
        result = 31 * result + minDirectCountAgentId.hashCode();
        result = 31 * result + (int) (minDirectCount ^ (minDirectCount >>> 32));

        result = 31 * result + (int) (avgDirectMemoryUsed ^ (avgDirectMemoryUsed >>> 32));
        result = 31 * result + maxDirectMemoryUsedAgentId.hashCode();
        result = 31 * result + (int) (maxDirectMemoryUsed ^ (maxDirectMemoryUsed >>> 32));
        result = 31 * result + minDirectMemoryUsedAgentId.hashCode();
        result = 31 * result + (int) (minDirectMemoryUsed ^ (minDirectMemoryUsed >>> 32));

        result = 31 * result + (int) (avgMappedCount ^ (avgMappedCount >>> 32));
        result = 31 * result + maxMappedCountAgentId.hashCode();
        result = 31 * result + (int) (maxMappedCount ^ (maxMappedCount >>> 32));
        result = 31 * result + minMappedCountAgentId.hashCode();
        result = 31 * result + (int) (minMappedCount ^ (minMappedCount >>> 32));

        result = 31 * result + (int) (avgMappedMemoryUsed ^ (avgMappedMemoryUsed >>> 32));
        result = 31 * result + maxMappedMemoryUsedAgentId.hashCode();
        result = 31 * result + (int) (maxMappedMemoryUsed ^ (maxMappedMemoryUsed >>> 32));
        result = 31 * result + minMappedMemoryUsedAgentId.hashCode();
        result = 31 * result + (int) (minMappedMemoryUsed ^ (minMappedMemoryUsed >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "JoinDirectBufferBo{" +
                "id='" + id + '\'' +
                ", avgDirectCount=" + avgDirectCount +
                ", maxDirectCountAgentId='" + maxDirectCountAgentId + '\'' +
                ", maxDirectCount=" + maxDirectCount +
                ", minDirectCountAgentId='" + minDirectCountAgentId + '\'' +
                ", minDirectCount=" + minDirectCount +
                ", avgDirectMemoryUsed=" + avgDirectMemoryUsed +
                ", maxDirectMemoryUsedAgentId='" + maxDirectMemoryUsedAgentId + '\'' +
                ", maxDirectMemoryUsed=" + maxDirectMemoryUsed +
                ", minDirectMemoryUsedAgentId='" + minDirectMemoryUsedAgentId + '\'' +
                ", minDirectMemoryUsed=" + minDirectMemoryUsed +
                ", avgMappedCount=" + avgMappedCount +
                ", maxMappedCountAgentId='" + maxMappedCountAgentId + '\'' +
                ", maxMappedCount=" + maxMappedCount +
                ", minMappedCountAgentId='" + minMappedCountAgentId + '\'' +
                ", minMappedCount=" + minMappedCount +
                ", avgMappedMemoryUsed=" + avgMappedMemoryUsed +
                ", maxMappedMemoryUsedAgentId='" + maxMappedMemoryUsedAgentId + '\'' +
                ", maxMappedMemoryUsed=" + maxMappedMemoryUsed +
                ", minMappedMemoryUsedAgentId='" + minMappedMemoryUsedAgentId + '\'' +
                ", minMappedMemoryUsed=" + minMappedMemoryUsed +
                ", timestamp=" + timestamp +"(" + new Date(timestamp)+ ")" +
                '}';
    }
}
