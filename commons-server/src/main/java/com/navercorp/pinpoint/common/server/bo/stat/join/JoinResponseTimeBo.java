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
public class JoinResponseTimeBo implements JoinStatBo {

    public static final JoinResponseTimeBo EMPTY_JOIN_RESPONSE_TIME_BO = new JoinResponseTimeBo();
    public static final long UNCOLLECTED_VALUE = -1;


    private String id = UNKNOWN_ID;
    private long timestamp = Long.MIN_VALUE;
    private long avg = UNCOLLECTED_VALUE;
    private String maxAvgAgentId = UNKNOWN_AGENT;
    private long maxAvg = UNCOLLECTED_VALUE;
    private String minAvgAgentId = UNKNOWN_AGENT;
    private long minAvg = UNCOLLECTED_VALUE;

    public JoinResponseTimeBo() {
    }

    public JoinResponseTimeBo(String id, long timestamp, long avg, long minAvg, String minAvgAgentId, long maxAvg, String maxAvgAgentId) {
        this.id = id;
        this.timestamp = timestamp;
        this.avg = avg;
        this.maxAvgAgentId = maxAvgAgentId;
        this.maxAvg = maxAvg;
        this.minAvgAgentId = minAvgAgentId;
        this.minAvg = minAvg;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setAvg(long avg) {
        this.avg = avg;
    }

    public void setMaxAvgAgentId(String maxAvgAgentId) {
        this.maxAvgAgentId = maxAvgAgentId;
    }

    public void setMaxAvg(long maxAvg) {
        this.maxAvg = maxAvg;
    }

    public void setMinAvgAgentId(String minAvgAgentId) {
        this.minAvgAgentId = minAvgAgentId;
    }

    public void setMinAvg(long minAvg) {
        this.minAvg = minAvg;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public long getAvg() {
        return avg;
    }

    public String getMaxAvgAgentId() {
        return maxAvgAgentId;
    }

    public long getMaxAvg() {
        return maxAvg;
    }

    public String getMinAvgAgentId() {
        return minAvgAgentId;
    }

    public long getMinAvg() {
        return minAvg;
    }

    public static JoinResponseTimeBo joinResponseTimeBoList(List<JoinResponseTimeBo> joinResponseTimeBoList, Long timestamp) {
        final int boCount = joinResponseTimeBoList.size();

        if (boCount == 0) {
            return JoinResponseTimeBo.EMPTY_JOIN_RESPONSE_TIME_BO;
        }

        final JoinResponseTimeBo initJoinResponseTimeBo = joinResponseTimeBoList.get(0);
        long sumAvg = 0;
        long maxAvg = initJoinResponseTimeBo.getMaxAvg();
        String maxAvgAgentId = initJoinResponseTimeBo.getMaxAvgAgentId();
        long minAvg = initJoinResponseTimeBo.getMinAvg();
        String minAvgAgentId = initJoinResponseTimeBo.getMinAvgAgentId();

        for (JoinResponseTimeBo joinResponseTimeBo : joinResponseTimeBoList) {
            sumAvg += joinResponseTimeBo.getAvg();

            if (joinResponseTimeBo.getMaxAvg() > maxAvg) {
                maxAvg = joinResponseTimeBo.getMaxAvg();
                maxAvgAgentId = joinResponseTimeBo.getMaxAvgAgentId();
            }
            if (joinResponseTimeBo.getMinAvg() < minAvg) {
                minAvg = joinResponseTimeBo.getMinAvg();
                minAvgAgentId = joinResponseTimeBo.getMinAvgAgentId();
            }
        }

        final JoinResponseTimeBo newJoinResponseTimeBo = new JoinResponseTimeBo();
        newJoinResponseTimeBo.setId(initJoinResponseTimeBo.getId());
        newJoinResponseTimeBo.setTimestamp(timestamp);
        newJoinResponseTimeBo.setAvg(sumAvg / (long)boCount);
        newJoinResponseTimeBo.setMinAvg(minAvg);
        newJoinResponseTimeBo.setMinAvgAgentId(minAvgAgentId);
        newJoinResponseTimeBo.setMaxAvg(maxAvg);
        newJoinResponseTimeBo.setMaxAvgAgentId(maxAvgAgentId);

        return newJoinResponseTimeBo;
    }

    @Override
    public String toString() {
        return "JoinResponseTimeBo{" +
            "id='" + id + '\'' +
            ", timestamp=" + new Date(timestamp) +
            ", avg=" + avg +
            ", maxAvgAgentId='" + maxAvgAgentId + '\'' +
            ", maxAvg=" + maxAvg +
            ", minAvgAgentId='" + minAvgAgentId + '\'' +
            ", minAvg=" + minAvg +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinResponseTimeBo that = (JoinResponseTimeBo) o;

        if (timestamp != that.timestamp) return false;
        if (avg != that.avg) return false;
        if (maxAvg != that.maxAvg) return false;
        if (minAvg != that.minAvg) return false;
        if (!id.equals(that.id)) return false;
        if (!maxAvgAgentId.equals(that.maxAvgAgentId)) return false;
        return minAvgAgentId.equals(that.minAvgAgentId);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) (avg ^ (avg >>> 32));
        result = 31 * result + maxAvgAgentId.hashCode();
        result = 31 * result + (int) (maxAvg ^ (maxAvg >>> 32));
        result = 31 * result + minAvgAgentId.hashCode();
        result = 31 * result + (int) (minAvg ^ (minAvg >>> 32));
        return result;
    }
}
