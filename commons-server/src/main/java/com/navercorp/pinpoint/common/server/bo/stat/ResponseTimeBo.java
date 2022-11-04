/*
 * Copyright 2017 NAVER Corp.
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
 * @author Taejin Koo
 */
public class ResponseTimeBo extends AbstractAgentStatDataPoint {

    public static final long UNCOLLECTED_VALUE = UNCOLLECTED_LONG;

    private long avg = 0;
    private long max = 0;

    public ResponseTimeBo() {
        super(AgentStatType.RESPONSE_TIME);
    }

    public long getAvg() {
        return avg;
    }

    public void setAvg(long avg) {
        this.avg = avg;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ResponseTimeBo that = (ResponseTimeBo) o;

        if (avg != that.avg) return false;
        return max == that.max;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (avg ^ (avg >>> 32));
        result = 31 * result + (int) (max ^ (max >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ResponseTimeBo{" +
                "avg=" + avg +
                ", max=" + max +
                "} " + super.toString();
    }
}
