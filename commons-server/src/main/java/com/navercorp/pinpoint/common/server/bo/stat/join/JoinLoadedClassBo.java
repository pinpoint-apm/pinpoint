/*
 * Copyright 2020 NAVER Corp.
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

public class JoinLoadedClassBo implements JoinStatBo {
    public static final JoinLoadedClassBo EMPTY_JOIN_LOADED_CLASS_BO = new JoinLoadedClassBo();
    public static final long UNCOLLECTED_VALUE = -1;

    private String id = UNKNOWN_ID;
    private long timestamp = Long.MIN_VALUE;

    private long avgLoadedClass = UNCOLLECTED_VALUE;
    private String maxLoadedClassAgentId = UNKNOWN_AGENT;
    private long maxLoadedClass = UNCOLLECTED_VALUE;
    private String minLoadedClassAgentId = UNKNOWN_AGENT;
    private long minLoadedClass = UNCOLLECTED_VALUE;

    private long avgUnloadedClass = UNCOLLECTED_VALUE;
    private String maxUnloadedClassAgentId = UNKNOWN_AGENT;
    private long maxUnloadedClass = UNCOLLECTED_VALUE;
    private String minUnloadedClassAgentId = UNKNOWN_AGENT;
    private long minUnloadedClass = UNCOLLECTED_VALUE;

    public JoinLoadedClassBo() {
    }

    public JoinLoadedClassBo(String id, long avgLoadedClass, long maxLoadedClass, String maxLoadedClassAgentId, long minLoadedClass, String minLoadedClassAgentId,
                             long avgUnloadedClass, long maxUnloadedClass, String maxUnloadedClassAgentId, long minUnloadedClass, String minUnloadedClassAgentId,
                             long timestamp) {
        this.id = id;
        this.avgLoadedClass = avgLoadedClass;
        this.maxLoadedClass = maxLoadedClass;
        this.maxLoadedClassAgentId = maxLoadedClassAgentId;
        this.minLoadedClass = minLoadedClass;
        this.minLoadedClassAgentId = minLoadedClassAgentId;
        this.avgUnloadedClass = avgUnloadedClass;
        this.maxUnloadedClass = maxUnloadedClass;
        this.maxUnloadedClassAgentId = maxUnloadedClassAgentId;
        this.minUnloadedClass = minUnloadedClass;
        this.minUnloadedClassAgentId = minUnloadedClassAgentId;
        this.timestamp = timestamp;
    }

    public static JoinLoadedClassBo joinLoadedClassBoList(List<JoinLoadedClassBo> joinLoadedClassBoList, Long timestamp) {
        int boCount = joinLoadedClassBoList.size();

        if (joinLoadedClassBoList.size() == 0) {
            return EMPTY_JOIN_LOADED_CLASS_BO;
        }
        JoinLoadedClassBo newJoinLoadedClassBo = new JoinLoadedClassBo();
        JoinLoadedClassBo initJoinLoadedClassBo = joinLoadedClassBoList.get(0);

        newJoinLoadedClassBo.setId(initJoinLoadedClassBo.getId());
        newJoinLoadedClassBo.setTimestamp(timestamp);

        long sumLoadedClass = 0L;
        String maxLoadedClassAgentId = initJoinLoadedClassBo.getMaxLoadedClassAgentId();
        long maxLoadedClass = initJoinLoadedClassBo.getMaxLoadedClass();
        String minLoadedClassAgentId = initJoinLoadedClassBo.getMinLoadedClassAgentId();
        long minLoadedClass = initJoinLoadedClassBo.getMinLoadedClass();

        long sumUnloadedClass = 0L;
        String maxUnloadedClassAgentId = initJoinLoadedClassBo.getMaxUnloadedClassAgentId();
        long maxUnloadedClass = initJoinLoadedClassBo.getMaxUnloadedClass();
        String minUnloadedClassAgentId = initJoinLoadedClassBo.getMinUnloadedClassAgentId();
        long minUnloadedClass = initJoinLoadedClassBo.getMinUnloadedClass();

        for (JoinLoadedClassBo joinLoadedClassBo : joinLoadedClassBoList) {
            sumLoadedClass += joinLoadedClassBo.getAvgLoadedClass();
            if (joinLoadedClassBo.getMaxLoadedClass() > maxLoadedClass) {
                maxLoadedClass = joinLoadedClassBo.getMaxLoadedClass();
                maxLoadedClassAgentId = joinLoadedClassBo.getMaxLoadedClassAgentId();
            }
            if (joinLoadedClassBo.getMinLoadedClass() < minLoadedClass) {
                minLoadedClass = joinLoadedClassBo.getMinLoadedClass();
                minLoadedClassAgentId = joinLoadedClassBo.getMinLoadedClassAgentId();
            }

            sumUnloadedClass += joinLoadedClassBo.getAvgUnloadedClass();
            if (joinLoadedClassBo.getMaxUnloadedClass() > maxUnloadedClass) {
                maxUnloadedClass = joinLoadedClassBo.getMaxUnloadedClass();
                maxUnloadedClassAgentId = joinLoadedClassBo.getMaxUnloadedClassAgentId();
            }
            if (joinLoadedClassBo.getMinUnloadedClass() < minUnloadedClass) {
                minUnloadedClass = joinLoadedClassBo.getMinUnloadedClass();
                minUnloadedClassAgentId = joinLoadedClassBo.getMinUnloadedClassAgentId();
            }
        }

        newJoinLoadedClassBo.setAvgLoadedClass((sumLoadedClass / boCount));
        newJoinLoadedClassBo.setMaxLoadedClass(maxLoadedClass);
        newJoinLoadedClassBo.setMaxLoadedClassAgentId(maxLoadedClassAgentId);
        newJoinLoadedClassBo.setMinLoadedClass(minLoadedClass);
        newJoinLoadedClassBo.setMinLoadedClassAgentId(minLoadedClassAgentId);

        newJoinLoadedClassBo.setAvgUnloadedClass((sumUnloadedClass / boCount));
        newJoinLoadedClassBo.setMaxUnloadedClass(maxUnloadedClass);
        newJoinLoadedClassBo.setMaxUnloadedClassAgentId(maxUnloadedClassAgentId);
        newJoinLoadedClassBo.setMinUnloadedClass(minUnloadedClass);
        newJoinLoadedClassBo.setMinUnloadedClassAgentId(minUnloadedClassAgentId);

        return newJoinLoadedClassBo;
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

    public long getAvgLoadedClass() {
        return avgLoadedClass;
    }

    public void setAvgLoadedClass(long avgLoadedClass) {
        this.avgLoadedClass = avgLoadedClass;
    }

    public String getMaxLoadedClassAgentId() {
        return maxLoadedClassAgentId;
    }

    public void setMaxLoadedClassAgentId(String maxLoadedClassAgentId) {
        this.maxLoadedClassAgentId = maxLoadedClassAgentId;
    }

    public long getMaxLoadedClass() {
        return maxLoadedClass;
    }

    public void setMaxLoadedClass(long maxLoadedClass) {
        this.maxLoadedClass = maxLoadedClass;
    }

    public String getMinLoadedClassAgentId() {
        return minLoadedClassAgentId;
    }

    public void setMinLoadedClassAgentId(String minLoadedClassAgentId) {
        this.minLoadedClassAgentId = minLoadedClassAgentId;
    }

    public long getMinLoadedClass() {
        return minLoadedClass;
    }

    public void setMinLoadedClass(long minLoadedClass) {
        this.minLoadedClass = minLoadedClass;
    }

    public long getAvgUnloadedClass() {
        return avgUnloadedClass;
    }

    public void setAvgUnloadedClass(long avgUnloadedClass) {
        this.avgUnloadedClass = avgUnloadedClass;
    }

    public String getMaxUnloadedClassAgentId() {
        return maxUnloadedClassAgentId;
    }

    public void setMaxUnloadedClassAgentId(String maxUnloadedClassAgentId) {
        this.maxUnloadedClassAgentId = maxUnloadedClassAgentId;
    }

    public long getMaxUnloadedClass() {
        return maxUnloadedClass;
    }

    public void setMaxUnloadedClass(long maxUnloadedClass) {
        this.maxUnloadedClass = maxUnloadedClass;
    }

    public String getMinUnloadedClassAgentId() {
        return minUnloadedClassAgentId;
    }

    public void setMinUnloadedClassAgentId(String minUnloadedClassAgentId) {
        this.minUnloadedClassAgentId = minUnloadedClassAgentId;
    }

    public long getMinUnloadedClass() {
        return minUnloadedClass;
    }

    public void setMinUnloadedClass(long minUnloadedClass) {
        this.minUnloadedClass = minUnloadedClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinLoadedClassBo that = (JoinLoadedClassBo) o;

        if (timestamp != that.timestamp) return false;
        if (avgLoadedClass != that.avgLoadedClass) return false;
        if (maxLoadedClass != that.maxLoadedClass) return false;
        if (minLoadedClass != that.minLoadedClass) return false;
        if (avgUnloadedClass != that.avgUnloadedClass) return false;
        if (maxUnloadedClass != that.maxUnloadedClass) return false;
        if (avgUnloadedClass != that.avgUnloadedClass) return false;
        if (!id.equals(that.id)) return false;
        if (!maxLoadedClassAgentId.equals(that.maxLoadedClassAgentId)) return false;
        if (!minLoadedClassAgentId.equals(that.minLoadedClassAgentId)) return false;
        if (!maxUnloadedClassAgentId.equals(that.maxUnloadedClassAgentId)) return false;
        if (!minUnloadedClassAgentId.equals(that.minUnloadedClassAgentId)) return false;
        return minUnloadedClassAgentId.equals(that.minUnloadedClassAgentId);
    }


    @Override
    public int hashCode() {
        int result;

        result = id.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));

        result = 31 * result + (int) (avgLoadedClass ^ (avgLoadedClass >>> 32));
        result = 31 * result + maxLoadedClassAgentId.hashCode();
        result = 31 * result + (int) (maxLoadedClass ^ (maxLoadedClass >>> 32));
        result = 31 * result + minLoadedClassAgentId.hashCode();
        result = 31 * result + (int) (minLoadedClass ^ (minLoadedClass >>> 32));

        result = 31 * result + (int) (avgUnloadedClass ^ (avgUnloadedClass >>> 32));
        result = 31 * result + maxUnloadedClassAgentId.hashCode();
        result = 31 * result + (int) (maxUnloadedClass ^ (maxUnloadedClass >>> 32));
        result = 31 * result + minUnloadedClassAgentId.hashCode();
        result = 31 * result + (int) (minUnloadedClass ^ (minUnloadedClass >>> 32));

        return result;
    }

    @Override
    public String toString() {
        return "JoinLoadedClassBo{" +
                "id='" + id + '\'' +
                ", avgLoadedClass=" + avgLoadedClass +
                ", maxLoadedClassAgentId='" + maxLoadedClassAgentId + '\'' +
                ", maxLoadedClass=" + maxLoadedClass +
                ", minLoadedClassAgentId='" + minLoadedClassAgentId + '\'' +
                ", minLoadedClass=" + minLoadedClass +
                ", avgUnloadedClass=" + avgUnloadedClass +
                ", maxUnloadedClassAgentId='" + maxUnloadedClassAgentId + '\'' +
                ", maxUnloadedClass=" + maxUnloadedClass +
                ", minUnloadedClassAgentId='" + minUnloadedClassAgentId + '\'' +
                ", minUnloadedClass=" + minUnloadedClass +
                ", timestamp=" + timestamp +"(" + new Date(timestamp)+ ")" +
                '}';
    }
}
