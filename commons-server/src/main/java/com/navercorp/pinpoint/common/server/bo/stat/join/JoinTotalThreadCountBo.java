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

public class JoinTotalThreadCountBo implements JoinStatBo {
    public static final JoinTotalThreadCountBo EMPTY_TOTAL_THREAD_COUNT_BO = new JoinTotalThreadCountBo();
    public static final long UNCOLLECTED_VALUE = -1L;

    private String id = UNKNOWN_ID;
    private long timestamp = Long.MIN_VALUE;

    private long avgTotalThreadCount = UNCOLLECTED_VALUE;
    private String maxTotalThreadCountAgentId = UNKNOWN_AGENT;
    private long maxTotalThreadCount = UNCOLLECTED_VALUE;
    private String minTotalThreadCountAgentId = UNKNOWN_AGENT;
    private long minTotalThreadCount = UNCOLLECTED_VALUE;

    public JoinTotalThreadCountBo() {

    }

    public JoinTotalThreadCountBo(String id, long timestamp, long avgTotalThreadCount,
                                  long minTotalThreadCount, String minTotalThreadCountAgentId,
                                  long maxTotalThreadCount, String maxTotalThreadCountAgentId) {
        this.id = id;
        this.timestamp = timestamp;
        this.avgTotalThreadCount = avgTotalThreadCount;
        this.maxTotalThreadCount = maxTotalThreadCount;
        this.maxTotalThreadCountAgentId = maxTotalThreadCountAgentId;
        this.minTotalThreadCount = minTotalThreadCount;
        this.minTotalThreadCountAgentId = minTotalThreadCountAgentId;
    }
    @Override
    public long getTimestamp() { return timestamp; }

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public long getAvgTotalThreadCount() {return avgTotalThreadCount; }

    public void setAvgTotalThreadCount(long avgTotalThreadCount) { this.avgTotalThreadCount = avgTotalThreadCount; }

    public long getMaxTotalThreadCount() { return maxTotalThreadCount; }

    public void setMaxTotalThreadCount(long maxTotalThreadCount) { this.maxTotalThreadCount = maxTotalThreadCount; }

    public long getMinTotalThreadCount() { return minTotalThreadCount; }

    public void setMinTotalThreadCount(long minTotalThreadCount) { this.minTotalThreadCount = minTotalThreadCount; }

    public String getMaxTotalThreadCountAgentId() { return maxTotalThreadCountAgentId; }

    public void setMaxTotalThreadCountAgentId(String maxTotalThreadCountAgentId) { this.maxTotalThreadCountAgentId = maxTotalThreadCountAgentId; }

    public String getMinTotalThreadCountAgentId() { return minTotalThreadCountAgentId; }

    public void setMinTotalThreadCountAgentId(String minTotalThreadCountAgentId) { this.minTotalThreadCountAgentId = minTotalThreadCountAgentId; }

    public static JoinTotalThreadCountBo joinTotalThreadCountBoList(List<JoinTotalThreadCountBo> joinTotalThreadCountBoList, Long timestamp) {
        final int boCount = joinTotalThreadCountBoList.size();

        if (boCount == 0) {
            return JoinTotalThreadCountBo.EMPTY_TOTAL_THREAD_COUNT_BO;
        }

        final JoinTotalThreadCountBo initJoinTotalThreadCountBo = joinTotalThreadCountBoList.get(0);
        long sumAvg = 0;
        long maxTotalThreadCount = initJoinTotalThreadCountBo.getMaxTotalThreadCount();
        String maxTotalThreadCountAgentId = initJoinTotalThreadCountBo.getMaxTotalThreadCountAgentId();
        long minTotalThreadCount = initJoinTotalThreadCountBo.getMinTotalThreadCount();
        String minTotalThreadCountAgentId = initJoinTotalThreadCountBo.getMinTotalThreadCountAgentId();

        for (JoinTotalThreadCountBo joinTotalThreadCountBo : joinTotalThreadCountBoList) {
            sumAvg += joinTotalThreadCountBo.getAvgTotalThreadCount();

            if (joinTotalThreadCountBo.getMaxTotalThreadCount() > maxTotalThreadCount) {
                maxTotalThreadCount = joinTotalThreadCountBo.getMaxTotalThreadCount();
                maxTotalThreadCountAgentId = joinTotalThreadCountBo.getMaxTotalThreadCountAgentId();
            }
            if (joinTotalThreadCountBo.getMinTotalThreadCount() < minTotalThreadCount) {
                minTotalThreadCount = joinTotalThreadCountBo.getMinTotalThreadCount();
                minTotalThreadCountAgentId = joinTotalThreadCountBo.getMinTotalThreadCountAgentId();
            }
        }

        final JoinTotalThreadCountBo newJoinTotalThreadCountBo = new JoinTotalThreadCountBo();
        newJoinTotalThreadCountBo.setId(initJoinTotalThreadCountBo.getId());
        newJoinTotalThreadCountBo.setTimestamp(timestamp);
        newJoinTotalThreadCountBo.setAvgTotalThreadCount(sumAvg / boCount);
        newJoinTotalThreadCountBo.setMinTotalThreadCount(minTotalThreadCount);
        newJoinTotalThreadCountBo.setMinTotalThreadCountAgentId(minTotalThreadCountAgentId);
        newJoinTotalThreadCountBo.setMaxTotalThreadCount(maxTotalThreadCount);
        newJoinTotalThreadCountBo.setMaxTotalThreadCountAgentId(maxTotalThreadCountAgentId);

        return newJoinTotalThreadCountBo;
    }
    @Override
    public String toString() {
        return "JoinTotalThreadCountBo{" +
                "id='" + id + '\'' +
                ", avgTotalThreadCount=" + avgTotalThreadCount +
                ", maxTotalThreadCountAgentId='" + maxTotalThreadCountAgentId + '\'' +
                ", maxTotalThreadCount=" + maxTotalThreadCount +
                ", minTotalThreadCountAgentId='" + minTotalThreadCountAgentId + '\'' +
                ", minTotalThreadCount=" + minTotalThreadCount +
                ", timestamp=" + timestamp +"(" + new Date(timestamp)+ ")" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinTotalThreadCountBo that = (JoinTotalThreadCountBo) o;

        if (timestamp != that.timestamp) return false;
        if (avgTotalThreadCount != that.avgTotalThreadCount) return false;
        if (maxTotalThreadCount != that.maxTotalThreadCount) return false;
        if (minTotalThreadCount != that.minTotalThreadCount) return false;
        if (!id.equals(that.id)) return false;
        if (!maxTotalThreadCountAgentId.equals(that.maxTotalThreadCountAgentId)) return false;
        return minTotalThreadCountAgentId.equals(that.minTotalThreadCountAgentId);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) (avgTotalThreadCount ^ (avgTotalThreadCount >>> 32));
        result = 31 * result + maxTotalThreadCountAgentId.hashCode();
        result = 31 * result + (int) (maxTotalThreadCount ^ (maxTotalThreadCount >>> 32));
        result = 31 * result + minTotalThreadCountAgentId.hashCode();
        result = 31 * result + (int) (minTotalThreadCount ^ (minTotalThreadCount >>> 32));

        return result;
    }
}
