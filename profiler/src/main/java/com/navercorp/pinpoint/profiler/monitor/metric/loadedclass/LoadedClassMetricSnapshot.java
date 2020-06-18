/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.metric.loadedclass;

public class LoadedClassMetricSnapshot {
    private final int loadedClassCount;
    private final long unloadedClassCount;

    public LoadedClassMetricSnapshot(int loadedClassCount, long unloadedClassCount) {
        this.loadedClassCount = loadedClassCount;
        this.unloadedClassCount = unloadedClassCount;
    }

    public int getLoadedClassCount() { return loadedClassCount; }

    public long getUnloadedClassCount() { return unloadedClassCount; }
}
