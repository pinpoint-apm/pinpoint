/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.vertx.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventApiIdAwareAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;

/**
 * @author jaehong.kim
 */
public class ContextImplExecuteBlockingInterceptor extends SpanEventApiIdAwareAroundInterceptorForPlugin {

    public ContextImplExecuteBlockingInterceptor(final TraceContext traceContext) {
        super(traceContext);
    }

    @Override
    public Trace currentTrace() {
        return traceContext.currentRawTraceObject();
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args) {
        if (!validate(args)) {
            return;
        }

        final AsyncContextAccessorHandlers handlers = getAsyncContextAccessorHandlers(args);
        if (handlers.blockingCodeHandler != null || handlers.resultHandler != null) {
            // make asynchronous trace-id
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            if (handlers.blockingCodeHandler != null) {
                // blockingCodeHandler
                handlers.blockingCodeHandler._$PINPOINT$_setAsyncContext(asyncContext);
                if (isDebug) {
                    logger.debug("Set asyncContext to ContextImpl.executeBlocking blockingCodeHandler. asyncContext={}", asyncContext);
                }
            }

            if (handlers.resultHandler != null) {
                // resultHandler.
                handlers.resultHandler._$PINPOINT$_setAsyncContext(asyncContext);
                if (isDebug) {
                    logger.debug("Set asyncContext to ContextImpl.executeBlocking resultHandler. asyncContext={}", asyncContext);
                }
            }
        }
    }

    private boolean validate(final Object[] args) {
        if (ArrayUtils.getLength(args) < 2) {
            return false;
        }
        return true;
    }

    private AsyncContextAccessorHandlers getAsyncContextAccessorHandlers(final Object[] args) {
        final AsyncContextAccessorHandlers handlers = new AsyncContextAccessorHandlers();
        int length = ArrayUtils.getLength(args);
        if (length == 2) {
            // Action<T> action, Handler<AsyncResult<T>> resultHandler
            if (args[1] instanceof AsyncContextAccessor) {
                handlers.resultHandler = (AsyncContextAccessor) args[1];
                return handlers;
            }
        } else if (length == 3) {
            // Handler<Future<T>> blockingCodeHandler, boolean ordered, Handler<AsyncResult<T>> resultHandler
            // Handler<Future<T>> blockingCodeHandler, TaskQueue queue, Handler<AsyncResult<T>> resultHandler
            if (args[0] instanceof AsyncContextAccessor) {
                handlers.blockingCodeHandler = (AsyncContextAccessor) args[0];
            }
            if (args[2] instanceof AsyncContextAccessor) {
                handlers.resultHandler = (AsyncContextAccessor) args[2];
            }
        } else if (length == 4) {
            // ContextInternal context, Handler<Promise<T>> blockingCodeHandler, WorkerPool workerPool, TaskQueue queue
            if (args[1] instanceof AsyncContextAccessor) {
                handlers.blockingCodeHandler = (AsyncContextAccessor) args[1];
            }
        }

        return handlers;
    }

    @Override
    public void afterTrace(Trace trace, SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (trace.canSampled()) {
            recorder.recordApiId(apiId);
            recorder.recordServiceType(VertxConstants.VERTX_INTERNAL);
            recorder.recordException(throwable);
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
    }

    private static class AsyncContextAccessorHandlers {
        private AsyncContextAccessor blockingCodeHandler;
        private AsyncContextAccessor resultHandler;
    }
}