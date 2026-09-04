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

import com.navercorp.pinpoint.plugin.ktor.KtorConstants;
import com.navercorp.pinpoint.plugin.ktor.KtorPluginConfig;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestRecorder;

import java.util.concurrent.atomic.AtomicBoolean;

public class KtorClientTraceHolder {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());

    private final AsyncContext asyncContext;
    private final Object request;
    private final MethodDescriptor methodDescriptor;
    private final KtorPluginConfig config;
    private final ClientRequestRecorder<Object> requestRecorder;
    private final AtomicBoolean asyncClosed = new AtomicBoolean(false);
    private volatile boolean attached;

    public KtorClientTraceHolder(
            AsyncContext asyncContext,
            Object request,
            MethodDescriptor methodDescriptor,
            KtorPluginConfig config,
            ClientRequestRecorder<Object> requestRecorder
    ) {
        this.asyncContext = asyncContext;
        this.request = request;
        this.methodDescriptor = methodDescriptor;
        this.config = config;
        this.requestRecorder = requestRecorder;
    }

    public void markAttached() {
        this.attached = true;
    }

    public boolean isAttached() {
        return attached;
    }

    public void record(SpanEventRecorder recorder, Throwable throwable) {
        if (recorder == null) {
            logger.warn("Current span event recorder is null while finishing Ktor client trace");
            return;
        }

        try {
            requestRecorder.record(recorder, request, throwable);
            recorder.recordApi(methodDescriptor);
            recorder.recordException(config.isClientMarkError(), throwable);
        } catch (Throwable recordThrowable) {
            logger.warn("Failed to record Ktor client trace. {}", recordThrowable.getMessage(), recordThrowable);
        }
    }

    public void recordCompletion(Trace trace, Throwable throwable) {
        if (!asyncClosed.compareAndSet(false, true)) {
            return;
        }

        Trace ownedAsyncTrace = null;
        try {
            if (trace != null) {
                // The trace that is live at resume time belongs to whoever resumed the
                // coroutine (the coroutines plugin's continuation trace, or the original
                // caller). Only a span event is pushed onto it; it is never closed here.
                recordSpanEvent(trace, throwable);
                return;
            }

            // No trace is live on this thread (e.g. coroutines tracing is disabled and the
            // suspended call resumes on the client engine thread). The binder is provably
            // empty, so the async trace created below is exclusively owned by this holder:
            // record on it and close it, so the async chunk is flushed and the binder is
            // restored to its previous (empty) state.
            ownedAsyncTrace = asyncContext.continueAsyncTraceObject(true);
            if (ownedAsyncTrace == null) {
                logger.warn("Could not continue Ktor client async trace");
                return;
            }
            recordSpanEvent(ownedAsyncTrace, throwable);
        } catch (Throwable throwableInTrace) {
            logger.warn(
                    "Failed to record Ktor client completion span. {}",
                    throwableInTrace.getMessage(),
                    throwableInTrace
            );
        } finally {
            closeOwnedAsyncTrace(ownedAsyncTrace);
            finishAsyncState();
        }
    }

    private void recordSpanEvent(Trace trace, Throwable throwable) {
        boolean blockStarted = false;
        try {
            SpanEventRecorder recorder = trace.traceBlockBegin();
            blockStarted = true;
            recorder.recordServiceType(KtorConstants.KTOR_CLIENT_INTERNAL);
            record(recorder, throwable);
        } finally {
            if (blockStarted) {
                try {
                    trace.traceBlockEnd();
                } catch (Throwable endThrowable) {
                    logger.warn("Failed to end Ktor client completion block. {}", endThrowable.getMessage(), endThrowable);
                }
            }
        }
    }

    private void closeOwnedAsyncTrace(Trace ownedAsyncTrace) {
        if (ownedAsyncTrace == null) {
            return;
        }
        try {
            ownedAsyncTrace.close();
        } catch (Throwable closeThrowable) {
            logger.warn("Failed to close Ktor client async trace. {}", closeThrowable.getMessage(), closeThrowable);
        }
        // Restore the thread binder unconditionally: the binder slot is provably ours in
        // this path, so it must be cleared even when the trace close itself failed.
        try {
            asyncContext.close();
        } catch (Throwable binderThrowable) {
            logger.warn("Failed to close Ktor client async context. {}", binderThrowable.getMessage(), binderThrowable);
        }
    }

    public void cancelAsync() {
        if (!asyncClosed.compareAndSet(false, true)) {
            return;
        }
        // Synchronous completion path never bound the async trace here; calling
        // asyncContext.close() would clear the shared thread binder and break another plugin's
        // in-flight trace. Only finish() is safe in this path.
        finishAsyncState();
    }

    private void finishAsyncState() {
        try {
            asyncContext.finish();
        } catch (Throwable finishThrowable) {
            logger.warn("Failed to finish Ktor client async state. {}", finishThrowable.getMessage(), finishThrowable);
        }
    }
}
