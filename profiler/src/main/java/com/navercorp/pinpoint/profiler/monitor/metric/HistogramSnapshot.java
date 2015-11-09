/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.metric;

/**
 * @author emeroad
 */
public class HistogramSnapshot {
    private final short serviceType;
    private final long fastCount;
    private final long fastErrorCount;
    private final long normalCount;
    private final long normalErrorCount;
    private final long slowCount;
    private final long slowErrorCount;
    private final long verySlowCount;
    private final long verySlowErrorCount;

    public HistogramSnapshot(short serviceType, long fastCount, long fastErrorCount, long normalCount, long normalErrorCount, long slowCount, long slowErrorCount, long verySlowCount, long verySlowErrorCount) {
        this.serviceType = serviceType;
        this.fastCount = fastCount;
        this.fastErrorCount = fastErrorCount;
        this.normalCount = normalCount;
        this.normalErrorCount = normalErrorCount;
        this.slowCount = slowCount;
        this.slowErrorCount = slowErrorCount;
        this.verySlowCount = verySlowCount;
        this.verySlowErrorCount = verySlowErrorCount;
    }

    public short getServiceType() {
        return serviceType;
    }

    public long getFastCount() {
        return fastCount;
    }

    public long getNormalCount() {
        return normalCount;
    }

    public long getSlowCount() {
        return slowCount;
    }

    public long getVerySlowCount() {
        return verySlowCount;
    }

    public long getVerySlowErrorCount() {
        return verySlowErrorCount;
    }

    public long getFastErrorCount() {
        return fastErrorCount;
    }

    public long getNormalErrorCount() {
        return normalErrorCount;
    }

    public long getSlowErrorCount() {
        return slowErrorCount;
    }

    @Override
    public String toString() {
        return "HistogramSnapshot{" +
                "serviceType=" + serviceType +
                ", fastCount=" + fastCount +
                ", fastErrorCount=" + fastErrorCount +
                ", normalCount=" + normalCount +
                ", normalErrorCount=" + normalErrorCount +
                ", slowCount=" + slowCount +
                ", slowErrorCount=" + slowErrorCount +
                ", verySlowCount=" + verySlowCount +
                ", verySlowErrorCount=" + verySlowErrorCount +
                '}';
    }
}
