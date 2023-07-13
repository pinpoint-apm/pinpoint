/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.sampler.TraceSampler;
import com.navercorp.pinpoint.profiler.context.BaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.CallStackFactory;
import com.navercorp.pinpoint.profiler.context.DefaultBaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.LoggingBaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.SpanFactory;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionContextFactory;
import com.navercorp.pinpoint.profiler.context.exception.storage.ExceptionStorageFactory;
import com.navercorp.pinpoint.profiler.context.id.TraceRootFactory;
import com.navercorp.pinpoint.profiler.context.recorder.RecorderFactory;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.context.storage.UriStatStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class BaseTraceFactoryProvider implements Provider<BaseTraceFactory> {

    private final TraceRootFactory traceRootFactory;
    private final StorageFactory storageFactory;
    private final TraceSampler traceSampler;

    private final CallStackFactory<SpanEvent> callStackFactory;
    private final SpanFactory spanFactory;
    private final RecorderFactory recorderFactory;

    private final ActiveTraceRepository activeTraceRepository;
    private final ExceptionContextFactory exceptionContextFactory;
    private final UriStatStorage uriStatStorage;

    @Inject
    public BaseTraceFactoryProvider(TraceRootFactory traceRootFactory,
                                    StorageFactory storageFactory,
                                    TraceSampler traceSampler,
                                    CallStackFactory<SpanEvent> callStackFactory,
                                    SpanFactory spanFactory,
                                    RecorderFactory recorderFactory,
                                    ActiveTraceRepository activeTraceRepository,
                                    UriStatStorage uriStatStorage,
                                    ExceptionContextFactory exceptionContextFactory) {
        this.traceRootFactory = Objects.requireNonNull(traceRootFactory, "traceRootFactory");

        this.callStackFactory = Objects.requireNonNull(callStackFactory, "callStackFactory");
        this.storageFactory = Objects.requireNonNull(storageFactory, "storageFactory");
        this.traceSampler = Objects.requireNonNull(traceSampler, "traceSampler");

        this.spanFactory = Objects.requireNonNull(spanFactory, "spanFactory");
        this.recorderFactory = Objects.requireNonNull(recorderFactory, "recorderFactory");
        this.activeTraceRepository = Objects.requireNonNull(activeTraceRepository, "activeTraceRepository");
        this.exceptionContextFactory = Objects.requireNonNull(exceptionContextFactory, "exceptionContextFactory");
        this.uriStatStorage = Objects.requireNonNull(uriStatStorage, "uriStatStorage");
    }

    @Override
    public BaseTraceFactory get() {
        BaseTraceFactory baseTraceFactory = new DefaultBaseTraceFactory(traceRootFactory, callStackFactory, storageFactory, traceSampler,
                spanFactory, recorderFactory, activeTraceRepository, exceptionContextFactory, uriStatStorage);
        if (isDebugEnabled()) {
            baseTraceFactory = LoggingBaseTraceFactory.wrap(baseTraceFactory);
        }
        return baseTraceFactory;
    }


    private boolean isDebugEnabled() {
        final Logger logger = LogManager.getLogger(DefaultBaseTraceFactory.class);
        return logger.isDebugEnabled();
    }


}
