/*
 * Copyright 2017 NAVER Corp.
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
 * @author minwoo.jung
 */
public class JoinCpuLoadBo implements JoinStatBo {
    public static final JoinCpuLoadBo EMPTY_JOIN_CPU_LOAD_BO = new JoinCpuLoadBo();
    public static final double UNCOLLECTED_VALUE = -1;

    private String id = UNKNOWN_ID;
    private long timestamp = Long.MIN_VALUE;

    private JoinDoubleFieldBo jvmCpuLoadJoinValue = JoinDoubleFieldBo.UNCOLLECTED_FIELD_BO;
    private JoinDoubleFieldBo systemCpuLoadJoinValue = JoinDoubleFieldBo.UNCOLLECTED_FIELD_BO;

    public JoinCpuLoadBo() {
    }

    public JoinCpuLoadBo(String id, double jvmCpuLoad, double maxJvmCpuLoad, String maxJvmCpuAgentId, double minJvmCpuLoad, String minJvmCpuAgentId, double systemCpuLoad, double maxSystemCpuLoad, String maxSysCpuAgentId, double minSystemCpuLoad, String minSysCpuAgentId, long timestamp) {
        this(id, new JoinDoubleFieldBo(jvmCpuLoad, minJvmCpuLoad, minJvmCpuAgentId, maxJvmCpuLoad, maxJvmCpuAgentId),
                new JoinDoubleFieldBo(systemCpuLoad, minSystemCpuLoad, minSysCpuAgentId, maxSystemCpuLoad, maxSysCpuAgentId), timestamp);
    }

    public JoinCpuLoadBo(String id, JoinDoubleFieldBo jvmCpuLoadJoinValue, JoinDoubleFieldBo systemCpuLoadJoinValue, long timestamp) {
        this.id = id;
        this.jvmCpuLoadJoinValue = Objects.requireNonNull(jvmCpuLoadJoinValue, "jvmCpuLoadJoinValue");
        this.systemCpuLoadJoinValue = Objects.requireNonNull(systemCpuLoadJoinValue, "systemCpuLoadJoinValue");
        this.timestamp = timestamp;
    }

    public static void apply(JoinApplicationStatBo.Builder builder, List<JoinCpuLoadBo> joinCpuLoadBoList, Long timestamp) {
        builder.addCpuLoad(joinCpuLoadBoList(joinCpuLoadBoList, timestamp));
    }

    public static JoinCpuLoadBo joinCpuLoadBoList(List<JoinCpuLoadBo> joinCpuLoadBoList, Long timestamp) {
        if (joinCpuLoadBoList.isEmpty()) {
            return EMPTY_JOIN_CPU_LOAD_BO;
        }

        final List<JoinDoubleFieldBo> jvmCpuLoadFieldBoList = joinCpuLoadBoList.stream().map(JoinCpuLoadBo::getJvmCpuLoadJoinValue).collect(Collectors.toList());
        final JoinDoubleFieldBo jvmCpuLoadJoinValue = JoinDoubleFieldBo.merge(jvmCpuLoadFieldBoList);

        final List<JoinDoubleFieldBo> systemCpuLoadFieldBoList = joinCpuLoadBoList.stream().map(JoinCpuLoadBo::getSystemCpuLoadJoinValue).collect(Collectors.toList());
        final JoinDoubleFieldBo systenCpuLoadJoinValue = JoinDoubleFieldBo.merge(systemCpuLoadFieldBoList);

        JoinCpuLoadBo firstJoinCpuLoadBo = joinCpuLoadBoList.get(0);

        JoinCpuLoadBo newJoinCpuLoadBo = new JoinCpuLoadBo();
        newJoinCpuLoadBo.setId(firstJoinCpuLoadBo.getId());
        newJoinCpuLoadBo.setTimestamp(timestamp);
        newJoinCpuLoadBo.setJvmCpuLoadJoinValue(jvmCpuLoadJoinValue);
        newJoinCpuLoadBo.setSystemCpuLoadJoinValue(systenCpuLoadJoinValue);
        return newJoinCpuLoadBo;
    }

    public void setId(String id) {
        this.id = id;
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

    public JoinDoubleFieldBo getJvmCpuLoadJoinValue() {
        return jvmCpuLoadJoinValue;
    }

    public void setJvmCpuLoadJoinValue(JoinDoubleFieldBo jvmCpuLoadJoinValue) {
        this.jvmCpuLoadJoinValue = jvmCpuLoadJoinValue;
    }

    public JoinDoubleFieldBo getSystemCpuLoadJoinValue() {
        return systemCpuLoadJoinValue;
    }

    public void setSystemCpuLoadJoinValue(JoinDoubleFieldBo systemCpuLoadJoinValue) {
        this.systemCpuLoadJoinValue = systemCpuLoadJoinValue;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JoinCpuLoadBo{");
        sb.append("id='").append(id).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", jvmCpuLoadJoinValue=").append(jvmCpuLoadJoinValue);
        sb.append(", systemCpuLoadJoinValue=").append(systemCpuLoadJoinValue);
        sb.append('}');
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinCpuLoadBo that = (JoinCpuLoadBo) o;

        if (timestamp != that.timestamp) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (jvmCpuLoadJoinValue != null ? !jvmCpuLoadJoinValue.equals(that.jvmCpuLoadJoinValue) : that.jvmCpuLoadJoinValue != null) return false;
        return systemCpuLoadJoinValue != null ? systemCpuLoadJoinValue.equals(that.systemCpuLoadJoinValue) : that.systemCpuLoadJoinValue == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (jvmCpuLoadJoinValue != null ? jvmCpuLoadJoinValue.hashCode() : 0);
        result = 31 * result + (systemCpuLoadJoinValue != null ? systemCpuLoadJoinValue.hashCode() : 0);
        return result;
    }
}
