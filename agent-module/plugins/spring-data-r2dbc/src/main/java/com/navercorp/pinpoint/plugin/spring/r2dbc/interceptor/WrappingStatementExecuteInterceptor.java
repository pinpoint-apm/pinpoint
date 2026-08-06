/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.ResultReplaceAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.plugin.reactorsupport.SeamPublisherWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Wrapping variant of {@link StatementExecuteInterceptor} (PoC, config-gated by
 * {@code profiler.spring.data.r2dbc.wrap.publisher}). Records the same execute span event, but
 * instead of injecting the {@link AsyncContext} into the returned publisher's accessor field it
 * replaces the return value with a wrapped publisher that delivers every signal inside the
 * async trace — no reactor-core field write, no dependency on the reactor plugin's per-operator
 * relay for this seam.
 */
public class WrappingStatementExecuteInterceptor implements ResultReplaceAroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    protected final TraceContext traceContext;
    protected final MethodDescriptor methodDescriptor;
    private final int maxSqlBindValueSize;

    public WrappingStatementExecuteInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, int maxSqlBindValueSize) {
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext");
        this.methodDescriptor = Objects.requireNonNull(methodDescriptor, "methodDescriptor");
        this.maxSqlBindValueSize = maxSqlBindValueSize;
    }

    @Override
    public void before(Object target, Class<?> returnType, Object[] args) {
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            recordStatement(recorder, target);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    // same recording as StatementExecuteInterceptor.doInBeforeTrace
    private void recordStatement(SpanEventRecorder recorder, Object target) {
        DatabaseInfo databaseInfo = (target instanceof DatabaseInfoAccessor) ? ((DatabaseInfoAccessor) target)._$PINPOINT$_getDatabaseInfo() : null;
        if (databaseInfo == null) {
            databaseInfo = UnKnownDatabaseInfo.INSTANCE;
        }

        recorder.recordDatabaseInfo(databaseInfo, true);

        ParsingResult parsingResult = null;
        if (target instanceof ParsingResultAccessor) {
            parsingResult = ((ParsingResultAccessor) target)._$PINPOINT$_getParsingResult();
        }
        Map<Integer, String> bindValue = null;
        if (target instanceof BindValueAccessor) {
            bindValue = ((BindValueAccessor) target)._$PINPOINT$_getBindValue();
        }
        if (bindValue != null) {
            String bindString = toBindVariable(bindValue);
            recorder.recordSqlParsingResult(parsingResult, bindString);
        } else {
            recorder.recordSqlParsingResult(parsingResult);
        }
        clean(target);
    }

    private String toBindVariable(Map<Integer, String> bindValue) {
        return traceContext.getJdbcContext().getBindVariableService().bindVariableToString(bindValue, maxSqlBindValueSize);
    }

    private void clean(Object target) {
        if (target instanceof BindValueAccessor) {
            ((BindValueAccessor) target)._$PINPOINT$_setBindValue(new HashMap<>());
        }
    }

    @Override
    public Object after(Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return result;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordException(throwable);
            recorder.recordApi(methodDescriptor);

            if (throwable != null || !SeamPublisherWrapper.isWrappable(result)) {
                return result;
            }

            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            final Object wrapped = SeamPublisherWrapper.wrap(result, asyncContext);
            if (isDebug) {
                logger.debug("Wrapped result publisher. asyncContext={}", asyncContext);
            }
            return wrapped;
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
            return result;
        } finally {
            trace.traceBlockEnd();
        }
    }
}
