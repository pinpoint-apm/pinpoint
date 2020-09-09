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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Hyunjoon Cho
 */
public class JoinContainerBo implements JoinStatBo {

    public static final JoinContainerBo EMPTY_JOIN_CONTAINER_BO = new JoinContainerBo();
    public static final double UNCOLLECTED_PERCENT_USAGE = -1D;
    public static final long UNCOLLECTED_MEMORY = -1;

    private String id = UNKNOWN_ID;
    private long timestamp = Long.MIN_VALUE;

    private JoinDoubleFieldBo userCpuUsageJoinValue = JoinDoubleFieldBo.UNCOLLECTED_FIELD_BO;
    private JoinDoubleFieldBo systemCpuUsageJoinValue = JoinDoubleFieldBo.UNCOLLECTED_FIELD_BO;
    private JoinLongFieldBo memoryMaxJoinValue = JoinLongFieldBo.UNCOLLECTED_FIELD_BO;
    private JoinLongFieldBo memoryUsageJoinValue = JoinLongFieldBo.UNCOLLECTED_FIELD_BO;

    public JoinContainerBo(){
    }

    public JoinContainerBo(String id, double avgUserCpuUsage, double maxUserCpuUsage, String maxUserCpuUsageAgentId, double minUserCpuUsage, String minUserCpuUsageAgentId
            , double avgSystemCpuUsage, double maxSystemCpuUsage, String maxSystemCpuUsageAgentId, double minSystemCpuUsage, String minSystemCpuUsageAgentId
            , long avgMemoryMax, long maxMemoryMax, String maxMemoryMaxAgentId, long minMemoryMax, String minMemoryMaxAgentId
            , long avgMemoryUsage, long maxMemoryUsage, String maxMemoryUsageAgentId, long minMemoryUsage, String minMemoryUsageAgentId
            , long timestamp) {
        this(id,
                new JoinDoubleFieldBo(avgUserCpuUsage, minUserCpuUsage, minUserCpuUsageAgentId, maxUserCpuUsage, maxUserCpuUsageAgentId),
                new JoinDoubleFieldBo(avgSystemCpuUsage, minSystemCpuUsage, minSystemCpuUsageAgentId, maxSystemCpuUsage, maxSystemCpuUsageAgentId),
                new JoinLongFieldBo(avgMemoryMax, minMemoryMax, minMemoryMaxAgentId, maxMemoryMax, maxMemoryMaxAgentId),
                new JoinLongFieldBo(avgMemoryUsage, minMemoryUsage, minMemoryUsageAgentId, maxMemoryUsage, maxMemoryUsageAgentId), timestamp);
    }

    public JoinContainerBo(String id, JoinDoubleFieldBo userCpuUsageJoinValue, JoinDoubleFieldBo systemCpuUsageJoinValue,
                              JoinLongFieldBo memoryMaxJoinValue, JoinLongFieldBo memoryUsageJoinValue, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
        this.userCpuUsageJoinValue = Objects.requireNonNull(userCpuUsageJoinValue, "userCpuUsageJoinValue");
        this.systemCpuUsageJoinValue = Objects.requireNonNull(systemCpuUsageJoinValue, "systemCpuUsageJoinValue");
        this.memoryMaxJoinValue = Objects.requireNonNull(memoryMaxJoinValue, "memoryMaxJoinValue");
        this.memoryUsageJoinValue = Objects.requireNonNull(memoryUsageJoinValue, "memoryUsageJoinValue");
    }

    public static void apply(JoinApplicationStatBo.Builder builder, List<JoinContainerBo> joinContainerBoList, Long timestamp) {
        builder.addContainer(joinContainerBoList(joinContainerBoList, timestamp));
    }

    public static JoinContainerBo joinContainerBoList(List<JoinContainerBo> joinContainerBoList, Long timestamp) {
        if (joinContainerBoList.isEmpty()) {
            return EMPTY_JOIN_CONTAINER_BO;
        }

        List<JoinDoubleFieldBo> userCpuUsageFieldBoList = joinContainerBoList.stream().map(e -> e.getUserCpuUsageJoinValue()).collect(Collectors.toList());
        JoinDoubleFieldBo userCpuUsageJoinValue = JoinDoubleFieldBo.merge(userCpuUsageFieldBoList);

        List<JoinDoubleFieldBo> systemCpuUsageFieldBoList = joinContainerBoList.stream().map(e -> e.getSystemCpuUsageJoinValue()).collect(Collectors.toList());
        JoinDoubleFieldBo systemCpuUsageJoinValue = JoinDoubleFieldBo.merge(systemCpuUsageFieldBoList);

        List<JoinLongFieldBo> memoryMaxFieldBoList = joinContainerBoList.stream().map(e -> e.getMemoryMaxJoinValue()).collect(Collectors.toList());
        JoinLongFieldBo memoryMaxJoinValue = JoinLongFieldBo.merge(memoryMaxFieldBoList);

        List<JoinLongFieldBo> memoryUsageFieldBoList = joinContainerBoList.stream().map(e -> e.getMemoryUsageJoinValue()).collect(Collectors.toList());
        JoinLongFieldBo memoryUsageJoinValue = JoinLongFieldBo.merge(memoryUsageFieldBoList);

        JoinContainerBo firstJoinContainerBo = joinContainerBoList.get(0);

        JoinContainerBo newJoinContainerBo = new JoinContainerBo();
        newJoinContainerBo.setId(firstJoinContainerBo.getId());
        newJoinContainerBo.setTimestamp(timestamp);
        newJoinContainerBo.setUserCpuUsageJoinValue(userCpuUsageJoinValue);
        newJoinContainerBo.setSystemCpuUsageJoinValue(systemCpuUsageJoinValue);
        newJoinContainerBo.setMemoryMaxJoinValue(memoryMaxJoinValue);
        newJoinContainerBo.setMemoryUsageJoinValue(memoryUsageJoinValue);
        return newJoinContainerBo;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }

    public JoinDoubleFieldBo getUserCpuUsageJoinValue(){
        return userCpuUsageJoinValue;
    }

    public void setUserCpuUsageJoinValue(JoinDoubleFieldBo userCpuUsageJoinValue){
        this.userCpuUsageJoinValue = userCpuUsageJoinValue;
    }

    public JoinDoubleFieldBo getSystemCpuUsageJoinValue(){
        return systemCpuUsageJoinValue;
    }

    public void setSystemCpuUsageJoinValue(JoinDoubleFieldBo systemCpuUsageJoinValue){
        this.systemCpuUsageJoinValue = systemCpuUsageJoinValue;
    }

    public JoinLongFieldBo getMemoryMaxJoinValue() {
        return memoryMaxJoinValue;
    }

    public void setMemoryMaxJoinValue(JoinLongFieldBo memoryMaxJoinValue) {
        this.memoryMaxJoinValue = memoryMaxJoinValue;
    }

    public JoinLongFieldBo getMemoryUsageJoinValue() {
        return memoryUsageJoinValue;
    }

    public void setMemoryUsageJoinValue(JoinLongFieldBo memoryUsageJoinValue){
        this.memoryUsageJoinValue = memoryUsageJoinValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinContainerBo that = (JoinContainerBo) o;

        if (timestamp != that.timestamp) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (userCpuUsageJoinValue != null ? !userCpuUsageJoinValue.equals(that.userCpuUsageJoinValue) : that.userCpuUsageJoinValue != null) return false;
        if (systemCpuUsageJoinValue != null ? !systemCpuUsageJoinValue.equals(that.systemCpuUsageJoinValue) : that.systemCpuUsageJoinValue != null) return false;
        if (memoryMaxJoinValue != null ? !memoryMaxJoinValue.equals(that.memoryMaxJoinValue) : that.memoryMaxJoinValue != null) return false;
        return memoryUsageJoinValue != null ? memoryUsageJoinValue.equals(that.memoryUsageJoinValue) : that.memoryUsageJoinValue == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (userCpuUsageJoinValue != null ? userCpuUsageJoinValue.hashCode() : 0);
        result = 31 * result + (systemCpuUsageJoinValue != null ? systemCpuUsageJoinValue.hashCode() : 0);
        result = 31 * result + (memoryMaxJoinValue != null ? memoryMaxJoinValue.hashCode() : 0);
        result = 31 * result + (memoryUsageJoinValue != null ? memoryUsageJoinValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JoinContainerBo{");
        sb.append("id='").append(id).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", userCpuUsageJoinValue=").append(userCpuUsageJoinValue);
        sb.append(", systemCpuUsageJoinValue=").append(systemCpuUsageJoinValue);
        sb.append(", memoryMaxJoinValue=").append(memoryMaxJoinValue);
        sb.append(", memoryUsageJoinValue=").append(memoryUsageJoinValue);
        sb.append('}');
        return sb.toString();
    }

}