/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo.stat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class AgentUriStatBo implements AgentStatDataPoint {

    private String agentId;
    private long startTimestamp;

    private long timestamp;
    private byte bucketVersion;

    private List<EachUriStatBo> eachUriStatBoList = new ArrayList<>();

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public long getStartTimestamp() {
        return startTimestamp;
    }

    @Override
    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public byte getBucketVersion() {
        return bucketVersion;
    }

    public void setBucketVersion(byte bucketVersion) {
        this.bucketVersion = bucketVersion;
    }

    @Override
    public AgentStatType getAgentStatType() {
        return AgentStatType.URI;
    }

    public boolean addEachUriStatBo(EachUriStatBo eachUriStatBo) {
        return eachUriStatBoList.add(eachUriStatBo);
    }

    public boolean addAllEachUriStatBo(Collection<EachUriStatBo> eachUriStatBos) {
        return eachUriStatBoList.addAll(eachUriStatBos);
    }

    public boolean removeEachUriStatBo(EachUriStatBo eachUriStatBo) {
        return eachUriStatBoList.remove(eachUriStatBo);
    }

    public List<EachUriStatBo> getEachUriStatBoList() {
        return eachUriStatBoList;
    }

    public void setEachUriStatBoList(List<EachUriStatBo> eachUriStatBoList) {
        this.eachUriStatBoList = eachUriStatBoList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentUriStatBo that = (AgentUriStatBo) o;

        if (startTimestamp != that.startTimestamp) return false;
        if (timestamp != that.timestamp) return false;
        if (bucketVersion != that.bucketVersion) return false;
        if (agentId != null ? !agentId.equals(that.agentId) : that.agentId != null) return false;
        return eachUriStatBoList != null ? eachUriStatBoList.equals(that.eachUriStatBoList) : that.eachUriStatBoList == null;
    }

    @Override
    public int hashCode() {
        int result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) bucketVersion;
        result = 31 * result + (eachUriStatBoList != null ? eachUriStatBoList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentUriStatBo{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", startTimestamp=").append(startTimestamp);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", bucketVersion=").append(bucketVersion);
        sb.append(", eachUriStatBoList=").append(eachUriStatBoList);
        sb.append('}');
        return sb.toString();
    }

}
