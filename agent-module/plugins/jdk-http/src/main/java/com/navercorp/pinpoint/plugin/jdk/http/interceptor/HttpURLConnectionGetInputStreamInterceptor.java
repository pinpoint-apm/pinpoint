/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdk.http.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;
import com.navercorp.pinpoint.plugin.jdk.http.JdkHttpConstants;

public class HttpURLConnectionGetInputStreamInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;

    public HttpURLConnectionGetInputStreamInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        entryTraceScope(trace);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            // do not log result
            logger.afterInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        leavyTraceScope(trace);
    }

    private boolean entryTraceScope(Trace trace) {
        TraceScope scope = trace.getScope(JdkHttpConstants.TRACE_SCOPE_NAME_GET_INPUT_STREAM);
        if (scope == null) {
            trace.addScope(JdkHttpConstants.TRACE_SCOPE_NAME_GET_INPUT_STREAM);
            scope = trace.getScope(JdkHttpConstants.TRACE_SCOPE_NAME_GET_INPUT_STREAM);
        }
        if (scope != null) {
            final boolean ok = scope.tryEnter();
            if (!ok) {
                if (isDebug) {
                    logger.debug("Try to startRecording response failed, tryEnter scope failed");
                }
            }
            return ok;
        } else {
            if (isDebug) {
                logger.debug("Try to startRecording response failed, getOrAdd scope failed");
            }
            return false;
        }
    }

    private void leavyTraceScope(Trace trace) {
        if (!ScopeUtils.leaveScope(trace, JdkHttpConstants.TRACE_SCOPE_NAME_GET_INPUT_STREAM)) {
            if (isDebug) {
                logger.debug("Try to  learRecordingResponseStatus failed, canLeave scope returned false");
            }
        }
    }
}
