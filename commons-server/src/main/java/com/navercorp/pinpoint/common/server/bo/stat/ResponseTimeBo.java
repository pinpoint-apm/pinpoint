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
public class ResponseTimeBo extends AbstractStatDataPoint {

    private final long avg;
    private final long max;

    public ResponseTimeBo(DataPoint point, long avg, long max) {
        super(point);
        this.avg = avg;
        this.max = max;
    }

    @Override
    public AgentStatType getAgentStatType() {
        return AgentStatType.RESPONSE_TIME;
    }

    public long getAvg() {
        return avg;
    }
    public long getMax() {
        return max;
    }

    @Override
    public String toString() {
        return "ResponseTimeBo{" +
                "point=" + point +
                ", avg=" + avg +
                ", max=" + max +
                '}';
    }
}
