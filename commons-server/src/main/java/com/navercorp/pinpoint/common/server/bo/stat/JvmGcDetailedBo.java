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
public class JvmGcDetailedBo extends AbstractAgentStatDataPoint {

    public static final long UNCOLLECTED_VALUE = UNCOLLECTED_LONG;
    public static final double UNCOLLECTED_PERCENTAGE = UNCOLLECTED_DOUBLE;

    private long gcNewCount = UNCOLLECTED_VALUE;
    private long gcNewTime = UNCOLLECTED_VALUE;
    private double codeCacheUsed = UNCOLLECTED_PERCENTAGE;
    private double newGenUsed = UNCOLLECTED_PERCENTAGE;
    private double oldGenUsed = UNCOLLECTED_PERCENTAGE;
    private double survivorSpaceUsed = UNCOLLECTED_PERCENTAGE;
    private double permGenUsed = UNCOLLECTED_PERCENTAGE;
    private double metaspaceUsed = UNCOLLECTED_PERCENTAGE;

    public JvmGcDetailedBo() {
        super(AgentStatType.JVM_GC_DETAILED);
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
        if (!super.equals(o)) return false;

        JvmGcDetailedBo that = (JvmGcDetailedBo) o;

        if (gcNewCount != that.gcNewCount) return false;
        if (gcNewTime != that.gcNewTime) return false;
        if (Double.compare(that.codeCacheUsed, codeCacheUsed) != 0) return false;
        if (Double.compare(that.newGenUsed, newGenUsed) != 0) return false;
        if (Double.compare(that.oldGenUsed, oldGenUsed) != 0) return false;
        if (Double.compare(that.survivorSpaceUsed, survivorSpaceUsed) != 0) return false;
        if (Double.compare(that.permGenUsed, permGenUsed) != 0) return false;
        return Double.compare(that.metaspaceUsed, metaspaceUsed) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
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
                "gcNewCount=" + gcNewCount +
                ", gcNewTime=" + gcNewTime +
                ", codeCacheUsed=" + codeCacheUsed +
                ", newGenUsed=" + newGenUsed +
                ", oldGenUsed=" + oldGenUsed +
                ", survivorSpaceUsed=" + survivorSpaceUsed +
                ", permGenUsed=" + permGenUsed +
                ", metaspaceUsed=" + metaspaceUsed +
                "} " + super.toString();
    }
}
