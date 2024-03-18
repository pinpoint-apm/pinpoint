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

package com.navercorp.pinpoint.profiler.monitor.metric.rpc;

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

    public HistogramSnapshot(short serviceType, long fastCount, long normalCount, long slowCount, long verySlowCount, long fastErrorCount, long normalErrorCount, long slowErrorCount, long verySlowErrorCount) {
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

    public long getFastErrorCount() {
        return fastErrorCount;
    }

    public long getNormalCount() {
        return normalCount;
    }

    public long getNormalErrorCount() {
        return normalErrorCount;
    }

    public long getSlowCount() {
        return slowCount;
    }

    public long getSlowErrorCount() {
        return slowErrorCount;
    }

    public long getVerySlowCount() {
        return verySlowCount;
    }

    public long getVerySlowErrorCount() {
        return verySlowErrorCount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("serviceType=").append(serviceType);
        sb.append(", fastCount=").append(fastCount);
        sb.append(", fastErrorCount=").append(fastErrorCount);
        sb.append(", normalCount=").append(normalCount);
        sb.append(", normalErrorCount=").append(normalErrorCount);
        sb.append(", slowCount=").append(slowCount);
        sb.append(", slowErrorCount=").append(slowErrorCount);
        sb.append(", verySlowCount=").append(verySlowCount);
        sb.append(", verySlowErrorCount=").append(verySlowErrorCount);
        sb.append('}');
        return sb.toString();
    }
}
