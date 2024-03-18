/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.recorder;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.profiler.context.AsyncContextFactory;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SqlCountService;
import com.navercorp.pinpoint.profiler.context.errorhandler.IgnoreErrorHandler;
import com.navercorp.pinpoint.profiler.context.exception.ExceptionRecorder;
import com.navercorp.pinpoint.profiler.context.exception.ExceptionRecorderFactory;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataService;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultRecorderFactory implements RecorderFactory {

    private final StringMetaDataService stringMetaDataService;
    private final SqlMetaDataService sqlMetaDataService;
    private final Provider<AsyncContextFactory> asyncContextFactoryProvider;
    private final IgnoreErrorHandler errorHandler;

    private final ExceptionRecorderFactory exceptionRecorderFactory;
    private final SqlCountService sqlCountService;

    @Inject
    public DefaultRecorderFactory(Provider<AsyncContextFactory> asyncContextFactoryProvider,
                                  StringMetaDataService stringMetaDataService,
                                  SqlMetaDataService sqlMetaDataService,
                                  IgnoreErrorHandler errorHandler,
                                  ExceptionRecorderFactory exceptionRecorderFactory,
                                  SqlCountService sqlCountService) {
        this.asyncContextFactoryProvider = Objects.requireNonNull(asyncContextFactoryProvider, "asyncContextFactoryProvider");
        this.stringMetaDataService = Objects.requireNonNull(stringMetaDataService, "stringMetaDataService");
        this.sqlMetaDataService = Objects.requireNonNull(sqlMetaDataService, "sqlMetaDataService");
        this.errorHandler = Objects.requireNonNull(errorHandler, "errorHandler");
        this.exceptionRecorderFactory = Objects.requireNonNull(exceptionRecorderFactory, "exceptionRecorderFactory");
        this.sqlCountService = Objects.requireNonNull(sqlCountService, "sqlCountService");
    }

    @Override
    public SpanRecorder newSpanRecorder(Span span) {
        Objects.requireNonNull(span, "span");

        ExceptionRecorder exceptionRecorder = exceptionRecorderFactory.newService(span.getTraceRoot());
        return new DefaultSpanRecorder(span, stringMetaDataService, sqlMetaDataService, errorHandler, exceptionRecorder);
    }

    @Override
    public SpanRecorder newTraceRootSpanRecorder(TraceRoot traceRoot) {
        Objects.requireNonNull(traceRoot, "traceRoot");

        return new TraceRootSpanRecorder(traceRoot);
    }

    @Override
    public SpanRecorder newDisableSpanRecorder(LocalTraceRoot traceRoot) {
        Objects.requireNonNull(traceRoot, "traceRoot");

        return new DisableSpanRecorder(traceRoot, errorHandler);
    }

    @Override
    public WrappedSpanEventRecorder newWrappedSpanEventRecorder(TraceRoot traceRoot) {
        Objects.requireNonNull(traceRoot, "traceRoot");

        final AsyncContextFactory asyncContextFactory = asyncContextFactoryProvider.get();
        ExceptionRecorder exceptionRecorder = exceptionRecorderFactory.newService(traceRoot);

        return new WrappedSpanEventRecorder(traceRoot, asyncContextFactory,
                stringMetaDataService, sqlMetaDataService, errorHandler, exceptionRecorder, sqlCountService);
    }

    @Override
    public WrappedSpanEventRecorder newWrappedSpanEventRecorder(TraceRoot traceRoot, AsyncState asyncState) {
        Objects.requireNonNull(traceRoot, "traceRoot");
        Objects.requireNonNull(asyncState, "asyncState");

        final AsyncContextFactory asyncContextFactory = asyncContextFactoryProvider.get();
        ExceptionRecorder exceptionRecorder = exceptionRecorderFactory.newService(traceRoot);

        return new WrappedSpanEventRecorder(traceRoot, asyncContextFactory, asyncState,
                stringMetaDataService, sqlMetaDataService, errorHandler, exceptionRecorder, sqlCountService);
    }

    @Override
    public WrappedSpanEventRecorder newChildTraceSpanEventRecorder(TraceRoot traceRoot) {
        Objects.requireNonNull(traceRoot, "traceRoot");

        final AsyncContextFactory asyncContextFactory = asyncContextFactoryProvider.get();
        ExceptionRecorder exceptionRecorder = exceptionRecorderFactory.newService(traceRoot);

        return new ChildTraceSpanEventRecorder(traceRoot, asyncContextFactory,
                stringMetaDataService, sqlMetaDataService, errorHandler, exceptionRecorder, sqlCountService);
    }

    @Override
    public DisableSpanEventRecorder newDisableSpanEventRecorder(LocalTraceRoot traceRoot, AsyncState asyncState) {
        Objects.requireNonNull(traceRoot, "traceRoot");
        Objects.requireNonNull(asyncState, "asyncState");

        return newDisableSpanEventRecorder0(traceRoot, asyncState);
    }

    @Override
    public DisableSpanEventRecorder newDisableSpanEventRecorder(LocalTraceRoot traceRoot) {
        Objects.requireNonNull(traceRoot, "traceRoot");

        return newDisableSpanEventRecorder0(traceRoot, null);
    }

    private DisableSpanEventRecorder newDisableSpanEventRecorder0(LocalTraceRoot traceRoot, AsyncState asyncState) {
        final AsyncContextFactory asyncContextFactory = asyncContextFactoryProvider.get();
        return new DisableSpanEventRecorder(traceRoot, asyncContextFactory, asyncState);
    }

    @Override
    public DisableSpanEventRecorder newDisableChildTraceSpanEventRecorder(LocalTraceRoot traceRoot, AsyncState asyncState) {
        Objects.requireNonNull(traceRoot, "traceRoot");
        Objects.requireNonNull(asyncState, "asyncState");

        final AsyncContextFactory asyncContextFactory = asyncContextFactoryProvider.get();
        return new DisableChildTraceSpanEventRecorder(traceRoot, asyncContextFactory, asyncState);
    }
}
