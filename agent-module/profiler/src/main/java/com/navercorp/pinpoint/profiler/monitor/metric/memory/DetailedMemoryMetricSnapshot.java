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

package com.navercorp.pinpoint.profiler.monitor.metric.memory;

/**
 * @author HyunGil Jeong
 */
public class DetailedMemoryMetricSnapshot {

    private final double newGenUsage;
    private final double oldGenUsage;
    private final double survivorSpaceUsage;
    private final double codeCacheUsage;
    private final double permGenUsage;
    private final double metaspaceUsage;

    DetailedMemoryMetricSnapshot(double edenSpaceMemoryUsage,
                                 double oldSpaceMemoryUsage,
                                 double survivorSpaceMemoryUsage,
                                 double codeCacheMemoryUsage,
                                 double permGenMemoryUsage,
                                 double metaspaceMemoryUsage) {
        this.newGenUsage = edenSpaceMemoryUsage;
        this.oldGenUsage = oldSpaceMemoryUsage;
        this.survivorSpaceUsage = survivorSpaceMemoryUsage;
        this.codeCacheUsage = codeCacheMemoryUsage;
        this.permGenUsage = permGenMemoryUsage;
        this.metaspaceUsage = metaspaceMemoryUsage;
    }

    public double getNewGenUsage() {
        return newGenUsage;
    }

    public double getOldGenUsage() {
        return oldGenUsage;
    }

    public double getCodeCacheUsage() {
        return codeCacheUsage;
    }

    public double getSurvivorSpaceUsage() {
        return survivorSpaceUsage;
    }

    public double getPermGenUsage() {
        return permGenUsage;
    }

    public double getMetaspaceUsage() {
        return metaspaceUsage;
    }
}
