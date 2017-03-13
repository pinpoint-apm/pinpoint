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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.profiler.context.id.AsyncIdGenerator;
import com.navercorp.pinpoint.profiler.context.BaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.CallStackFactory;
import com.navercorp.pinpoint.profiler.context.DefaultBaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.id.IdGenerator;
import com.navercorp.pinpoint.profiler.context.LoggingBaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.recorder.RecorderFactory;
import com.navercorp.pinpoint.profiler.context.SpanFactory;
import com.navercorp.pinpoint.profiler.context.ThreadLocalTraceFactory;
import com.navercorp.pinpoint.profiler.context.TraceFactory;
import com.navercorp.pinpoint.profiler.context.id.TraceIdFactory;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceFactory;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TraceFactoryProvider implements Provider<TraceFactory> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final StorageFactory storageFactory;
    private final Sampler sampler;
    private final IdGenerator idGenerator;
    private final TraceIdFactory traceIdFactory;
    private final AsyncIdGenerator asyncIdGenerator;

    private final ActiveTraceRepository activeTraceRepository;

    private final CallStackFactory callStackFactory;

    private final SpanFactory spanFactory;
    private final RecorderFactory recorderFactory;


    @Inject
    public TraceFactoryProvider(CallStackFactory callStackFactory, StorageFactory storageFactory, Sampler sampler, IdGenerator idGenerator, TraceIdFactory traceIdFactory, AsyncIdGenerator asyncIdGenerator,
                                Provider<ActiveTraceRepository> activeTraceRepositoryProvider, SpanFactory spanFactory, RecorderFactory recorderFactory) {
        if (callStackFactory == null) {
            throw new NullPointerException("callStackFactory must not be null");
        }
        if (storageFactory == null) {
            throw new NullPointerException("storageFactory must not be null");
        }
        if (sampler == null) {
            throw new NullPointerException("sampler must not be null");
        }
        if (idGenerator == null) {
            throw new NullPointerException("idGenerator must not be null");
        }
        if (traceIdFactory == null) {
            throw new NullPointerException("traceIdFactory must not be null");
        }

        if (asyncIdGenerator == null) {
            throw new NullPointerException("asyncIdGenerator must not be null");
        }
        if (activeTraceRepositoryProvider == null) {
            throw new NullPointerException("activeTraceRepositoryProvider must not be null");
        }
        if (spanFactory == null) {
            throw new NullPointerException("spanFactory must not be null");
        }
        if (recorderFactory == null) {
            throw new NullPointerException("recorderFactory must not be null");
        }

        this.callStackFactory = callStackFactory;
        this.storageFactory = storageFactory;
        this.sampler = sampler;
        this.idGenerator = idGenerator;
        this.traceIdFactory = traceIdFactory;
        this.asyncIdGenerator = asyncIdGenerator;
        this.activeTraceRepository = activeTraceRepositoryProvider.get();

        this.spanFactory = spanFactory;
        this.recorderFactory = recorderFactory;

    }

    @Override
    public TraceFactory get() {

        BaseTraceFactory baseTraceFactory = new DefaultBaseTraceFactory(callStackFactory, storageFactory, sampler, traceIdFactory, idGenerator,
                asyncIdGenerator, spanFactory, recorderFactory);
        if (isDebugEnabled()) {
            baseTraceFactory = LoggingBaseTraceFactory.wrap(baseTraceFactory);
        }

        TraceFactory traceFactory = new ThreadLocalTraceFactory(baseTraceFactory);
        if (this.activeTraceRepository != null) {
            this.logger.debug("enable ActiveTrace");
            traceFactory = ActiveTraceFactory.wrap(traceFactory, this.activeTraceRepository);
        }

        return traceFactory;
    }

    private boolean isDebugEnabled() {
        final Logger logger = LoggerFactory.getLogger(DefaultBaseTraceFactory.class);
        return logger.isDebugEnabled();
    }

}
