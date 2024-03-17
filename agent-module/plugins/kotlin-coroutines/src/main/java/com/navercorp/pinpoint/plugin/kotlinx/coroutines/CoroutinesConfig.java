/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kotlinx.coroutines;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author Taejin Koo
 */
public class CoroutinesConfig {

    private final boolean traceCoroutines;
    private final boolean traceCancelEvent;
    private final boolean recordThreadName;

    public CoroutinesConfig(ProfilerConfig config) {
        this.traceCoroutines = config.readBoolean("profiler.kotlin.coroutines.enable", false);
        this.traceCancelEvent = config.readBoolean("profiler.kotlin.coroutines.record.cancel", false);
        this.recordThreadName = config.readBoolean("profiler.kotlin.coroutines.record.threadName", false);
    }

    public boolean isTraceCoroutines() {
        return traceCoroutines;
    }

    public boolean isTraceCancelEvent() {
        return traceCancelEvent;
    }

    public boolean isRecordThreadName() {
        return recordThreadName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CoroutinesConfig{");
        sb.append("traceCoroutines=").append(traceCoroutines);
        sb.append(", traceCancelEvent=").append(traceCancelEvent);
        sb.append(", recordThreadName=").append(recordThreadName);
        sb.append('}');
        return sb.toString();
    }
}
