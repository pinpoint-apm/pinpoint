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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Roy Kim
 */
public class JoinFileDescriptorBo implements JoinStatBo {
    public static final JoinFileDescriptorBo EMPTY_JOIN_FILE_DESCRIPTOR_BO = new JoinFileDescriptorBo();
    public static final long UNCOLLECTED_VALUE = -1;

    private String id = UNKNOWN_ID;
    private long timestamp = Long.MIN_VALUE;
    private JoinLongFieldBo openFdCountJoinValue = JoinLongFieldBo.UNCOLLECTED_FIELD_BO;

    public JoinFileDescriptorBo() {
    }

    public JoinFileDescriptorBo(String id, long avgOpenFDCount, long maxOpenFDCount, String maxOpenFDCountAgentId, long minOpenFDCount, String minOpenFDCountAgentId, long timestamp) {
        this(id, new JoinLongFieldBo(avgOpenFDCount, minOpenFDCount, minOpenFDCountAgentId, maxOpenFDCount, maxOpenFDCountAgentId), timestamp);
    }

    public JoinFileDescriptorBo(String id, JoinLongFieldBo openFdCountJoinValue, long timestamp) {
        this.id = id;
        this.openFdCountJoinValue = Objects.requireNonNull(openFdCountJoinValue, "openFdCountJoinValue");
        this.timestamp = timestamp;
    }

    public static void apply(JoinApplicationStatBo.Builder builder, List<JoinFileDescriptorBo> joinFileDescriptorBoList, Long timestamp) {
        builder.addFileDescriptor(joinFileDescriptorBoList(joinFileDescriptorBoList, timestamp));
    }

    public static JoinFileDescriptorBo joinFileDescriptorBoList(List<JoinFileDescriptorBo> joinFileDescriptorBoList, Long timestamp) {

        if (joinFileDescriptorBoList.isEmpty()) {
            return EMPTY_JOIN_FILE_DESCRIPTOR_BO;
        }

        List<JoinLongFieldBo> openFdCountFieldBoList = joinFileDescriptorBoList.stream().map(e -> e.openFdCountJoinValue).collect(Collectors.toList());
        JoinLongFieldBo openFdCountJoinValue = JoinLongFieldBo.merge(openFdCountFieldBoList);

        final JoinFileDescriptorBo firstJoinFileDescriptorBo = joinFileDescriptorBoList.get(0);

        final JoinFileDescriptorBo newJoinFileDescriptorBo = new JoinFileDescriptorBo();
        newJoinFileDescriptorBo.setId(firstJoinFileDescriptorBo.getId());
        newJoinFileDescriptorBo.setTimestamp(timestamp);
        newJoinFileDescriptorBo.setOpenFdCountJoinValue(openFdCountJoinValue);

        return newJoinFileDescriptorBo;
    }

    public JoinLongFieldBo getOpenFdCountJoinValue() {
        return openFdCountJoinValue;
    }

    public void setOpenFdCountJoinValue(JoinLongFieldBo openFdCountJoinValue) {
        this.openFdCountJoinValue = openFdCountJoinValue;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinFileDescriptorBo that = (JoinFileDescriptorBo) o;

        if (timestamp != that.timestamp) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return openFdCountJoinValue != null ? openFdCountJoinValue.equals(that.openFdCountJoinValue) : that.openFdCountJoinValue == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (openFdCountJoinValue != null ? openFdCountJoinValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JoinFileDescriptorBo{");
        sb.append("id='").append(id).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", openFdCountJoinValue=").append(openFdCountJoinValue);
        sb.append('}');
        return sb.toString();
    }
}
