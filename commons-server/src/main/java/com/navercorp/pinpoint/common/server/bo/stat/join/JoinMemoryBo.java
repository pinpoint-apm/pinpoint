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
public class JoinMemoryBo implements JoinStatBo {
    public static final JoinMemoryBo EMPTY_JOIN_MEMORY_BO = new JoinMemoryBo();
    public static final long UNCOLLECTED_VALUE = -1;

    private String id = UNKNOWN_ID;
    private long timestamp = Long.MIN_VALUE;

    private long heapUsed = UNCOLLECTED_VALUE;
    private long minHeapUsed = UNCOLLECTED_VALUE;
    private long maxHeapUsed = UNCOLLECTED_VALUE;
    private String minHeapAgentId = UNKNOWN_AGENT;
    private String maxHeapAgentId = UNKNOWN_AGENT;

    private long nonHeapUsed = UNCOLLECTED_VALUE;
    private long minNonHeapUsed = UNCOLLECTED_VALUE;
    private long maxNonHeapUsed = UNCOLLECTED_VALUE;
    private String minNonHeapAgentId = UNKNOWN_AGENT;
    private String maxNonHeapAgentId = UNKNOWN_AGENT;

    public JoinMemoryBo() {
    }

    public JoinMemoryBo(String id, long timestamp, long heapUsed, long minHeapUsed, long maxHeapUsed, String minHeapAgentId, String maxHeapAgentId, long nonHeapUsed, long minNonHeapUsed, long maxNonHeapUsed, String minNonHeapAgentId, String maxNonHeapAgentId) {
        this.id = id;
        this.timestamp = timestamp;
        this.heapUsed = heapUsed;
        this.minHeapUsed = minHeapUsed;
        this.maxHeapUsed = maxHeapUsed;
        this.minHeapAgentId = minHeapAgentId;
        this.maxHeapAgentId = maxHeapAgentId;
        this.nonHeapUsed = nonHeapUsed;
        this.minNonHeapUsed = minNonHeapUsed;
        this.maxNonHeapUsed = maxNonHeapUsed;
        this.minNonHeapAgentId = minNonHeapAgentId;
        this.maxNonHeapAgentId = maxNonHeapAgentId;
    }

    public long getMinHeapUsed() {
        return minHeapUsed;
    }

    public void setMinHeapUsed(long minHeapUsed) {
        this.minHeapUsed = minHeapUsed;
    }

    public long getMaxHeapUsed() {
        return maxHeapUsed;
    }

    public void setMaxHeapUsed(long maxHeapUsed) {
        this.maxHeapUsed = maxHeapUsed;
    }

    public String getMinHeapAgentId() {
        return minHeapAgentId;
    }

    public void setMinHeapAgentId(String minHeapAgentId) {
        this.minHeapAgentId = minHeapAgentId;
    }

    public String getMaxHeapAgentId() {
        return maxHeapAgentId;
    }

    public void setMaxHeapAgentId(String maxHeapAgentId) {
        this.maxHeapAgentId = maxHeapAgentId;
    }

    public long getMinNonHeapUsed() {
        return minNonHeapUsed;
    }

    public void setMinNonHeapUsed(long minNonHeapUsed) {
        this.minNonHeapUsed = minNonHeapUsed;
    }

    public long getMaxNonHeapUsed() {
        return maxNonHeapUsed;
    }

    public void setMaxNonHeapUsed(long maxNonHeapUsed) {
        this.maxNonHeapUsed = maxNonHeapUsed;
    }

    public String getMinNonHeapAgentId() {
        return minNonHeapAgentId;
    }

    public void setMinNonHeapAgentId(String minNonHeapAgentId) {
        this.minNonHeapAgentId = minNonHeapAgentId;
    }

    public String getMaxNonHeapAgentId() {
        return maxNonHeapAgentId;
    }

    public void setMaxNonHeapAgentId(String maxNonHeapAgentId) {
        this.maxNonHeapAgentId = maxNonHeapAgentId;
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

    public void setHeapUsed(long heapUsed) {
        this.heapUsed = heapUsed;
    }

    public void setNonHeapUsed(long nonHeapUsed) {
        this.nonHeapUsed = nonHeapUsed;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getHeapUsed() {
        return heapUsed;
    }

    public long getNonHeapUsed() {
        return nonHeapUsed;
    }

    public static JoinMemoryBo joinMemoryBoList(List<JoinMemoryBo> joinMemoryBoList, Long timestamp) {
        final int boCount = joinMemoryBoList.size();

        if (boCount == 0) {
            return JoinMemoryBo.EMPTY_JOIN_MEMORY_BO;
        }

        final JoinMemoryBo initJoinMemoryBo = joinMemoryBoList.get(0);
        long sumHeapUsed = 0;
        long minHeapUsed = initJoinMemoryBo.getMinHeapUsed();
        long maxHeapUsed = initJoinMemoryBo.getMaxHeapUsed();
        String minHeapAgentId = initJoinMemoryBo.getMinHeapAgentId();
        String maxHeapAgentId = initJoinMemoryBo.getMaxHeapAgentId();
        long sumNonHeapUsed = 0;
        long minNonHeapUsed = initJoinMemoryBo.getMinNonHeapUsed();
        long maxNonHeapUsed = initJoinMemoryBo.getMaxNonHeapUsed();
        String minNonHeapAgentId = initJoinMemoryBo.getMinNonHeapAgentId();
        String maxNonHeapAgentId = initJoinMemoryBo.getMaxNonHeapAgentId();

        for (JoinMemoryBo joinMemoryBo : joinMemoryBoList) {
            sumHeapUsed += joinMemoryBo.getHeapUsed();
            if (joinMemoryBo.getMaxHeapUsed() > maxHeapUsed) {
                maxHeapUsed = joinMemoryBo.getMaxHeapUsed();
                maxHeapAgentId = joinMemoryBo.getMaxHeapAgentId();
            }
            if (joinMemoryBo.getMinHeapUsed() < minHeapUsed) {
                minHeapUsed = joinMemoryBo.getMinHeapUsed();
                minHeapAgentId = joinMemoryBo.getMinHeapAgentId();
            }

            sumNonHeapUsed += joinMemoryBo.getNonHeapUsed();
            if (joinMemoryBo.getMaxNonHeapUsed() > maxNonHeapUsed) {
                maxNonHeapUsed = joinMemoryBo.getMaxNonHeapUsed();
                maxNonHeapAgentId = joinMemoryBo.getMaxNonHeapAgentId();
            }
            if (joinMemoryBo.getMinNonHeapUsed() < minNonHeapUsed) {
                minNonHeapUsed = joinMemoryBo.getMinNonHeapUsed();
                minNonHeapAgentId = joinMemoryBo.getMinNonHeapAgentId();
            }
        }

        final JoinMemoryBo newJoinMemoryBo = new JoinMemoryBo();
        newJoinMemoryBo.setId(initJoinMemoryBo.getId());
        newJoinMemoryBo.setTimestamp(timestamp);
        newJoinMemoryBo.setHeapUsed(sumHeapUsed / (long)boCount);
        newJoinMemoryBo.setMinHeapUsed(minHeapUsed);
        newJoinMemoryBo.setMinHeapAgentId(minHeapAgentId);
        newJoinMemoryBo.setMaxHeapUsed(maxHeapUsed);
        newJoinMemoryBo.setMaxHeapAgentId(maxHeapAgentId);
        newJoinMemoryBo.setNonHeapUsed(sumNonHeapUsed / (long)boCount);
        newJoinMemoryBo.setMinNonHeapUsed(minNonHeapUsed);
        newJoinMemoryBo.setMinNonHeapAgentId(minNonHeapAgentId);
        newJoinMemoryBo.setMaxNonHeapUsed(maxNonHeapUsed);
        newJoinMemoryBo.setMaxNonHeapAgentId(maxNonHeapAgentId);

        return newJoinMemoryBo;
    }

    @Override
    public String toString() {
        return "JoinMemoryBo{" +
            "id='" + id + '\'' +
            ", heapUsed=" + heapUsed +
            ", minHeapUsed=" + minHeapUsed +
            ", maxHeapUsed=" + maxHeapUsed +
            ", minHeapAgentId='" + minHeapAgentId + '\'' +
            ", maxHeapAgentId='" + maxHeapAgentId + '\'' +
            ", nonHeapUsed=" + nonHeapUsed +
            ", minNonHeapUsed=" + minNonHeapUsed +
            ", maxNonHeapUsed=" + maxNonHeapUsed +
            ", minNonHeapAgentId='" + minNonHeapAgentId + '\'' +
            ", maxNonHeapAgentId='" + maxNonHeapAgentId + '\'' +
            ", timestamp=" + timestamp +"(" + new Date(timestamp)+ ")" +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinMemoryBo that = (JoinMemoryBo) o;

        if (timestamp != that.timestamp) return false;
        if (heapUsed != that.heapUsed) return false;
        if (minHeapUsed != that.minHeapUsed) return false;
        if (maxHeapUsed != that.maxHeapUsed) return false;
        if (nonHeapUsed != that.nonHeapUsed) return false;
        if (minNonHeapUsed != that.minNonHeapUsed) return false;
        if (maxNonHeapUsed != that.maxNonHeapUsed) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (minHeapAgentId != null ? !minHeapAgentId.equals(that.minHeapAgentId) : that.minHeapAgentId != null) return false;
        if (maxHeapAgentId != null ? !maxHeapAgentId.equals(that.maxHeapAgentId) : that.maxHeapAgentId != null) return false;
        if (minNonHeapAgentId != null ? !minNonHeapAgentId.equals(that.minNonHeapAgentId) : that.minNonHeapAgentId != null) return false;
        return maxNonHeapAgentId != null ? maxNonHeapAgentId.equals(that.maxNonHeapAgentId) : that.maxNonHeapAgentId == null;

    }
}
