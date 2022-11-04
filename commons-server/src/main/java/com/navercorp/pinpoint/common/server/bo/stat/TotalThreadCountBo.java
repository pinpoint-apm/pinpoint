/*
 * Copyright 2020 Naver Corp.
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

public class TotalThreadCountBo extends AbstractAgentStatDataPoint {
    public static final long UNCOLLECTED_VALUE = UNCOLLECTED_LONG;

    private long totalThreadCount = UNCOLLECTED_VALUE;


    public TotalThreadCountBo() {
        super(AgentStatType.TOTAL_THREAD);
    }

    public long getTotalThreadCount() {
        return totalThreadCount;
    }

    public void setTotalThreadCount(long totalThreadCount) {
        this.totalThreadCount = totalThreadCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TotalThreadCountBo that = (TotalThreadCountBo) o;

        return totalThreadCount == that.totalThreadCount;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (totalThreadCount ^ (totalThreadCount >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "TotalThreadCountBo{" +
                "totalThreadCount=" + totalThreadCount +
                "} " + super.toString();
    }
}
