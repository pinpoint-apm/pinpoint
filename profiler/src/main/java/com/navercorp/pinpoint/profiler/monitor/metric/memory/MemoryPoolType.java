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
public enum MemoryPoolType {
    SERIAL("Code Cache", "Eden Space", "Tenured Gen", "Survivor Space", "Perm Gen", "Metaspace"),
    PARALLEL("Code Cache", "PS Eden Space", "PS Old Gen", "PS Survivor Space", "PS Perm Gen", "Metaspace"),
    CMS("Code Cache", "Par Eden Space", "CMS Old Gen", "Par Survivor Space", "CMS Perm Gen", "Metaspace"),
    G1("Code Cache", "G1 Eden Space", "G1 Old Gen", "G1 Survivor Space", "G1 Perm Gen", "Metaspace");

    private final String codeCachePoolName;
    private final String edenSpacePoolName;
    private final String oldSpacePoolName;
    private final String survivorSpacePoolName;
    private final String permGenPoolName;
    private final String metaspacePoolName;

    MemoryPoolType(
            String codeCachePoolName, String edenSpacePoolName, String oldSpacePoolName,
            String survivorSpacePoolName, String permGenPoolName, String metaspacePoolName) {
        this.codeCachePoolName = codeCachePoolName;
        this.edenSpacePoolName = edenSpacePoolName;
        this.oldSpacePoolName = oldSpacePoolName;
        this.survivorSpacePoolName = survivorSpacePoolName;
        this.permGenPoolName = permGenPoolName;
        this.metaspacePoolName = metaspacePoolName;
    }

    public String codeCache() {
        return codeCachePoolName;
    }

    public String edenSpace() {
        return edenSpacePoolName;
    }

    public String oldSpace() {
        return oldSpacePoolName;
    }

    public String survivorSpace() {
        return survivorSpacePoolName;
    }

    public String permGen() {
        return permGenPoolName;
    }

    public String metaspace() {
        return metaspacePoolName;
    }
}
