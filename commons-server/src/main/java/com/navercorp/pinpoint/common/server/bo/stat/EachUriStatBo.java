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

/**
 * @author Taejin Koo
 */
public class EachUriStatBo implements AgentStatDataPoint {

    private String agentId;
    private long startTimestamp;

    private long timestamp;

    private String uri;
    private UriStatHistogram totalHistogram = new UriStatHistogram();
    private UriStatHistogram failedHistogram = null;

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

    @Override
    public AgentStatType getAgentStatType() {
        return AgentStatType.URI;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public UriStatHistogram getTotalHistogram() {
        return totalHistogram;
    }

    public void setTotalHistogram(UriStatHistogram totalHistogram) {
        this.totalHistogram = totalHistogram;
    }

    public UriStatHistogram getFailedHistogram() {
        return failedHistogram;
    }

    public void setFailedHistogram(UriStatHistogram failedHistogram) {
        this.failedHistogram = failedHistogram;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EachUriStatBo that = (EachUriStatBo) o;

        if (startTimestamp != that.startTimestamp) return false;
        if (timestamp != that.timestamp) return false;
        if (agentId != null ? !agentId.equals(that.agentId) : that.agentId != null) return false;
        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;
        if (totalHistogram != null ? !totalHistogram.equals(that.totalHistogram) : that.totalHistogram != null) return false;
        return failedHistogram != null ? failedHistogram.equals(that.failedHistogram) : that.failedHistogram == null;
    }

    @Override
    public int hashCode() {
        int result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + (totalHistogram != null ? totalHistogram.hashCode() : 0);
        result = 31 * result + (failedHistogram != null ? failedHistogram.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EachUriStatBo{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", startTimestamp=").append(startTimestamp);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", uri='").append(uri).append('\'');
        sb.append(", totalHistogram=").append(totalHistogram);
        sb.append(", failedHistogram=").append(failedHistogram);
        sb.append('}');
        return sb.toString();
    }
}
