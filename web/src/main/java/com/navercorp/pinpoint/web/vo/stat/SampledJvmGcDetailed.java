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

package com.navercorp.pinpoint.web.vo.stat;

import com.navercorp.pinpoint.web.vo.chart.Point;

/**
 * @author HyunGil Jeong
 */
public class SampledJvmGcDetailed implements SampledAgentStatDataPoint {

    private Point<Long, Long> gcNewCount;
    private Point<Long, Long> gcNewTime;
    private Point<Long, Double> codeCacheUsed;
    private Point<Long, Double> newGenUsed;
    private Point<Long, Double> oldGenUsed;
    private Point<Long, Double> survivorSpaceUsed;
    private Point<Long, Double> permGenUsed;
    private Point<Long, Double> metaspaceUsed;

    public Point<Long, Long> getGcNewCount() {
        return gcNewCount;
    }

    public void setGcNewCount(Point<Long, Long> gcNewCount) {
        this.gcNewCount = gcNewCount;
    }

    public Point<Long, Long> getGcNewTime() {
        return gcNewTime;
    }

    public void setGcNewTime(Point<Long, Long> gcNewTime) {
        this.gcNewTime = gcNewTime;
    }

    public Point<Long, Double> getCodeCacheUsed() {
        return codeCacheUsed;
    }

    public void setCodeCacheUsed(Point<Long, Double> codeCacheUsed) {
        this.codeCacheUsed = codeCacheUsed;
    }

    public Point<Long, Double> getNewGenUsed() {
        return newGenUsed;
    }

    public void setNewGenUsed(Point<Long, Double> newGenUsed) {
        this.newGenUsed = newGenUsed;
    }

    public Point<Long, Double> getOldGenUsed() {
        return oldGenUsed;
    }

    public void setOldGenUsed(Point<Long, Double> oldGenUsed) {
        this.oldGenUsed = oldGenUsed;
    }

    public Point<Long, Double> getSurvivorSpaceUsed() {
        return survivorSpaceUsed;
    }

    public void setSurvivorSpaceUsed(Point<Long, Double> survivorSpaceUsed) {
        this.survivorSpaceUsed = survivorSpaceUsed;
    }

    public Point<Long, Double> getPermGenUsed() {
        return permGenUsed;
    }

    public void setPermGenUsed(Point<Long, Double> permGenUsed) {
        this.permGenUsed = permGenUsed;
    }

    public Point<Long, Double> getMetaspaceUsed() {
        return metaspaceUsed;
    }

    public void setMetaspaceUsed(Point<Long, Double> metaspaceUsed) {
        this.metaspaceUsed = metaspaceUsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampledJvmGcDetailed that = (SampledJvmGcDetailed) o;

        if (gcNewCount != null ? !gcNewCount.equals(that.gcNewCount) : that.gcNewCount != null) return false;
        if (gcNewTime != null ? !gcNewTime.equals(that.gcNewTime) : that.gcNewTime != null) return false;
        if (codeCacheUsed != null ? !codeCacheUsed.equals(that.codeCacheUsed) : that.codeCacheUsed != null)
            return false;
        if (newGenUsed != null ? !newGenUsed.equals(that.newGenUsed) : that.newGenUsed != null) return false;
        if (oldGenUsed != null ? !oldGenUsed.equals(that.oldGenUsed) : that.oldGenUsed != null) return false;
        if (survivorSpaceUsed != null ? !survivorSpaceUsed.equals(that.survivorSpaceUsed) : that.survivorSpaceUsed != null)
            return false;
        if (permGenUsed != null ? !permGenUsed.equals(that.permGenUsed) : that.permGenUsed != null) return false;
        return metaspaceUsed != null ? metaspaceUsed.equals(that.metaspaceUsed) : that.metaspaceUsed == null;
    }

    @Override
    public int hashCode() {
        int result = gcNewCount != null ? gcNewCount.hashCode() : 0;
        result = 31 * result + (gcNewTime != null ? gcNewTime.hashCode() : 0);
        result = 31 * result + (codeCacheUsed != null ? codeCacheUsed.hashCode() : 0);
        result = 31 * result + (newGenUsed != null ? newGenUsed.hashCode() : 0);
        result = 31 * result + (oldGenUsed != null ? oldGenUsed.hashCode() : 0);
        result = 31 * result + (survivorSpaceUsed != null ? survivorSpaceUsed.hashCode() : 0);
        result = 31 * result + (permGenUsed != null ? permGenUsed.hashCode() : 0);
        result = 31 * result + (metaspaceUsed != null ? metaspaceUsed.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledJvmGcDetailed{");
        sb.append("gcNewCount=").append(gcNewCount);
        sb.append(", gcNewTime=").append(gcNewTime);
        sb.append(", codeCacheUsed=").append(codeCacheUsed);
        sb.append(", newGenUsed=").append(newGenUsed);
        sb.append(", oldGenUsed=").append(oldGenUsed);
        sb.append(", survivorSpaceUsed=").append(survivorSpaceUsed);
        sb.append(", permGenUsed=").append(permGenUsed);
        sb.append(", metaspaceUsed=").append(metaspaceUsed);
        sb.append('}');
        return sb.toString();
    }
}
