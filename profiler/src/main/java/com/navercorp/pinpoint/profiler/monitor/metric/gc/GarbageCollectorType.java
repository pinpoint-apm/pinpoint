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

package com.navercorp.pinpoint.profiler.monitor.metric.gc;

import com.navercorp.pinpoint.thrift.dto.TJvmGcType;

/**
 * @author HyunGil Jeong
 */
public enum GarbageCollectorType {
    SERIAL(TJvmGcType.SERIAL, "MarkSweepCompact", "Copy"),
    PARALLEL(TJvmGcType.PARALLEL, "PS MarkSweep", "PS Scavenge"),
    CMS(TJvmGcType.CMS, "ConcurrentMarkSweep", "ParNew"),
    G1(TJvmGcType.G1, "G1 Old Generation", "G1 Young Generation");

    private final TJvmGcType jvmGcType;
    private final String oldGenName;
    private final String newGenName;

    GarbageCollectorType(TJvmGcType jvmGcType, String oldGenName, String newGenName) {
        this.jvmGcType = jvmGcType;
        this.oldGenName = oldGenName;
        this.newGenName = newGenName;
    }

    public TJvmGcType jvmGcType() {
        return jvmGcType;
    }

    public String oldGenName() {
        return oldGenName;
    }

    public String newGenName() {
        return newGenName;
    }
}
