/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.navercorp.pinpoint.plugin.ktor.client;

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;

import java.util.concurrent.atomic.AtomicBoolean;

final class KtorClientParentTraceState {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());

    private final Trace trace;
    private final KtorClientTraceHolder holder;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    KtorClientParentTraceState(Trace trace, KtorClientTraceHolder holder) {
        this.trace = trace;
        this.holder = holder;
    }

    KtorClientTraceHolder getHolder() {
        return holder;
    }

    void finish(Throwable throwable) {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            holder.record(recorder, throwable);
        } catch (Throwable recorderThrowable) {
            logger.warn("Failed to get Ktor client parent span event recorder. {}", recorderThrowable.getMessage(), recorderThrowable);
        } finally {
            closeTraceBlock();
        }
    }

    void closeTraceBlock() {
        try {
            trace.traceBlockEnd();
        } catch (Throwable closeThrowable) {
            logger.warn("Failed to close Ktor client parent trace block. {}", closeThrowable.getMessage(), closeThrowable);
        }
    }
}
