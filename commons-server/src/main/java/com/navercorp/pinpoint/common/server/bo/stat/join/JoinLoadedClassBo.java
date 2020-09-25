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
import java.util.Objects;
import java.util.stream.Collectors;

public class JoinLoadedClassBo implements JoinStatBo {
    public static final JoinLoadedClassBo EMPTY_JOIN_LOADED_CLASS_BO = new JoinLoadedClassBo();
    public static final long UNCOLLECTED_VALUE = -1;

    private String id = UNKNOWN_ID;
    private long timestamp = Long.MIN_VALUE;

    private JoinLongFieldBo loadedClassJoinValue = JoinLongFieldBo.UNCOLLECTED_FIELD_BO;
    private JoinLongFieldBo unloadedClassJoinValue = JoinLongFieldBo.UNCOLLECTED_FIELD_BO;

    public JoinLoadedClassBo() {
    }

    public JoinLoadedClassBo(String id, long avgLoadedClass, long maxLoadedClass, String maxLoadedClassAgentId, long minLoadedClass, String minLoadedClassAgentId,
                             long avgUnloadedClass, long maxUnloadedClass, String maxUnloadedClassAgentId, long minUnloadedClass, String minUnloadedClassAgentId,
                             long timestamp) {
        this(id, new JoinLongFieldBo(avgLoadedClass, minLoadedClass, minLoadedClassAgentId, maxLoadedClass, maxLoadedClassAgentId),
                new JoinLongFieldBo(avgUnloadedClass, minUnloadedClass, minUnloadedClassAgentId, maxUnloadedClass, maxUnloadedClassAgentId), timestamp);
    }

    public JoinLoadedClassBo(String id, JoinLongFieldBo loadedClassJoinValue, JoinLongFieldBo unloadedClassJoinValue, long timestamp) {
        this.id = id;
        this.loadedClassJoinValue = Objects.requireNonNull(loadedClassJoinValue, "loadedClassJoinValue");
        this.unloadedClassJoinValue = Objects.requireNonNull(unloadedClassJoinValue, "unloadedClassJoinValue");
        this.timestamp = timestamp;
    }

    public static void apply(JoinApplicationStatBo.Builder builder, List<JoinLoadedClassBo> joinLoadedClassBoList, Long timestamp) {
        builder.addLoadedClass(joinLoadedClassBoList(joinLoadedClassBoList, timestamp));
    }

    public static JoinLoadedClassBo joinLoadedClassBoList(List<JoinLoadedClassBo> joinLoadedClassBoList, Long timestamp) {
        if (joinLoadedClassBoList.isEmpty()) {
            return EMPTY_JOIN_LOADED_CLASS_BO;
        }

        List<JoinLongFieldBo> loadedClassFieldBoList = joinLoadedClassBoList.stream().map(JoinLoadedClassBo::getLoadedClassJoinValue).collect(Collectors.toList());
        JoinLongFieldBo loadedClassJoinValue = JoinLongFieldBo.merge(loadedClassFieldBoList);

        List<JoinLongFieldBo> unloadedClassFieldBoList = joinLoadedClassBoList.stream().map(JoinLoadedClassBo::getUnloadedClassJoinValue).collect(Collectors.toList());
        JoinLongFieldBo unloadedClassJoinValue = JoinLongFieldBo.merge(unloadedClassFieldBoList);

        JoinLoadedClassBo firstJoinLoadedClassBo = joinLoadedClassBoList.get(0);

        JoinLoadedClassBo newJoinLoadedClassBo = new JoinLoadedClassBo();
        newJoinLoadedClassBo.setId(firstJoinLoadedClassBo.getId());
        newJoinLoadedClassBo.setTimestamp(timestamp);
        newJoinLoadedClassBo.setLoadedClassJoinValue(loadedClassJoinValue);
        newJoinLoadedClassBo.setUnloadedClassJoinValue(unloadedClassJoinValue);

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

    public JoinLongFieldBo getLoadedClassJoinValue() {
        return loadedClassJoinValue;
    }

    public void setLoadedClassJoinValue(JoinLongFieldBo loadedClassJoinValue) {
        this.loadedClassJoinValue = loadedClassJoinValue;
    }

    public JoinLongFieldBo getUnloadedClassJoinValue() {
        return unloadedClassJoinValue;
    }

    public void setUnloadedClassJoinValue(JoinLongFieldBo unloadedClassJoinValue) {
        this.unloadedClassJoinValue = unloadedClassJoinValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinLoadedClassBo that = (JoinLoadedClassBo) o;

        if (timestamp != that.timestamp) return false;
        if (!id.equals(that.id)) return false;
        if (loadedClassJoinValue != null ? !loadedClassJoinValue.equals(that.loadedClassJoinValue) : that.loadedClassJoinValue != null) return false;
        if (unloadedClassJoinValue != null ? !unloadedClassJoinValue.equals(that.unloadedClassJoinValue) : that.unloadedClassJoinValue != null) return false;

        return (unloadedClassJoinValue != null ? unloadedClassJoinValue.equals(that.unloadedClassJoinValue) : that.unloadedClassJoinValue == null);
    }


    @Override
    public int hashCode() {
        int result = (id != null) ? id.hashCode() : 0;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (loadedClassJoinValue != null ? loadedClassJoinValue.hashCode() : 0);
        result = 31 * result + (unloadedClassJoinValue != null ? unloadedClassJoinValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JoinLoadedClassBo{");
        sb.append("id='").append(id).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", loadedClassJoinValue=").append(loadedClassJoinValue);
        sb.append(", unloadedClassJoinValue=").append(unloadedClassJoinValue);
        sb.append('}');
        return sb.toString();
    }
}
