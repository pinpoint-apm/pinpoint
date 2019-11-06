/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.common.server.bo.stat;

/**
 * @author HyunGil Jeong
 */
public class JvmGcDetailedBo implements AgentStatDataPoint {

    public static final long UNCOLLECTED_VALUE = -1;
    public static final double UNCOLLECTED_PERCENTAGE = -1;

    private String agentId;
    private long startTimestamp;
    private long timestamp;
    private long gcNewCount = UNCOLLECTED_VALUE;
    private long gcNewTime = UNCOLLECTED_VALUE;
    private double codeCacheUsed = UNCOLLECTED_PERCENTAGE;
    private double newGenUsed = UNCOLLECTED_PERCENTAGE;
    private double oldGenUsed = UNCOLLECTED_PERCENTAGE;
    private double survivorSpaceUsed = UNCOLLECTED_PERCENTAGE;
    private double permGenUsed = UNCOLLECTED_PERCENTAGE;
    private double metaspaceUsed = UNCOLLECTED_PERCENTAGE;

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
        return AgentStatType.JVM_GC_DETAILED;
    }

    public long getGcNewCount() {
        return gcNewCount;
    }

    public void setGcNewCount(long gcNewCount) {
        this.gcNewCount = gcNewCount;
    }

    public long getGcNewTime() {
        return gcNewTime;
    }

    public void setGcNewTime(long gcNewTime) {
        this.gcNewTime = gcNewTime;
    }

    public double getCodeCacheUsed() {
        return codeCacheUsed;
    }

    public void setCodeCacheUsed(double codeCacheUsed) {
        this.codeCacheUsed = codeCacheUsed;
    }

    public double getNewGenUsed() {
        return newGenUsed;
    }

    public void setNewGenUsed(double newGenUsed) {
        this.newGenUsed = newGenUsed;
    }

    public double getOldGenUsed() {
        return oldGenUsed;
    }

    public void setOldGenUsed(double oldGenUsed) {
        this.oldGenUsed = oldGenUsed;
    }

    public double getSurvivorSpaceUsed() {
        return survivorSpaceUsed;
    }

    public void setSurvivorSpaceUsed(double survivorSpaceUsed) {
        this.survivorSpaceUsed = survivorSpaceUsed;
    }

    public double getPermGenUsed() {
        return permGenUsed;
    }

    public void setPermGenUsed(double permGenUsed) {
        this.permGenUsed = permGenUsed;
    }

    public double getMetaspaceUsed() {
        return metaspaceUsed;
    }

    public void setMetaspaceUsed(double metaspaceUsed) {
        this.metaspaceUsed = metaspaceUsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JvmGcDetailedBo that = (JvmGcDetailedBo) o;

        if (startTimestamp != that.startTimestamp) return false;
        if (timestamp != that.timestamp) return false;
        if (gcNewCount != that.gcNewCount) return false;
        if (gcNewTime != that.gcNewTime) return false;
        if (Double.compare(that.codeCacheUsed, codeCacheUsed) != 0) return false;
        if (Double.compare(that.newGenUsed, newGenUsed) != 0) return false;
        if (Double.compare(that.oldGenUsed, oldGenUsed) != 0) return false;
        if (Double.compare(that.survivorSpaceUsed, survivorSpaceUsed) != 0) return false;
        if (Double.compare(that.permGenUsed, permGenUsed) != 0) return false;
        if (Double.compare(that.metaspaceUsed, metaspaceUsed) != 0) return false;
        return agentId != null ? agentId.equals(that.agentId) : that.agentId == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) (gcNewCount ^ (gcNewCount >>> 32));
        result = 31 * result + (int) (gcNewTime ^ (gcNewTime >>> 32));
        temp = Double.doubleToLongBits(codeCacheUsed);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(newGenUsed);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(oldGenUsed);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(survivorSpaceUsed);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(permGenUsed);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(metaspaceUsed);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "JvmGcDetailedBo{" +
                "agentId='" + agentId + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", timestamp=" + timestamp +
                ", gcNewCount=" + gcNewCount +
                ", gcNewTime=" + gcNewTime +
                ", codeCacheUsed=" + codeCacheUsed +
                ", newGenUsed=" + newGenUsed +
                ", oldGenUsed=" + oldGenUsed +
                ", survivorSpaceUsed=" + survivorSpaceUsed +
                ", permGenUsed=" + permGenUsed +
                ", metaspaceUsed=" + metaspaceUsed +
                '}';
    }
}
