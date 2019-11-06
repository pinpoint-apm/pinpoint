/*
 * Copyright 2017 NAVER Corp.
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
 * @author Woonduk Kang(emeroad)
 */
public class ActiveTraceHistogram {

    private static final int UNCOLLECTED_COUNT = ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT;
    public static final ActiveTraceHistogram UNCOLLECTED = new ActiveTraceHistogram(UNCOLLECTED_COUNT, UNCOLLECTED_COUNT, UNCOLLECTED_COUNT, UNCOLLECTED_COUNT);
    public static final ActiveTraceHistogram EMPTY = new ActiveTraceHistogram(0, 0, 0, 0);

    private final int fastCount;
    private final int normalCount;
    private final int slowCount;
    private final int verySlowCount;

    public ActiveTraceHistogram(int fastCount, int normalCount, int slowCount, int verySlowCount) {
        this.fastCount = fastCount;
        this.normalCount = normalCount;
        this.slowCount = slowCount;
        this.verySlowCount = verySlowCount;
    }

    public int getFastCount() {
        return fastCount;
    }


    public int getNormalCount() {
        return normalCount;
    }

    public int getSlowCount() {
        return slowCount;
    }


    public int getVerySlowCount() {
        return verySlowCount;
    }

    @Override
    public String toString() {
        return "ActiveTraceHistogram{" +
                "fastCount=" + fastCount +
                ", normalCount=" + normalCount +
                ", slowCount=" + slowCount +
                ", verySlowCount=" + verySlowCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActiveTraceHistogram that = (ActiveTraceHistogram) o;

        if (fastCount != that.fastCount) return false;
        if (normalCount != that.normalCount) return false;
        if (slowCount != that.slowCount) return false;
        return verySlowCount == that.verySlowCount;
    }

    @Override
    public int hashCode() {
        int result = fastCount;
        result = 31 * result + normalCount;
        result = 31 * result + slowCount;
        result = 31 * result + verySlowCount;
        return result;
    }
}
