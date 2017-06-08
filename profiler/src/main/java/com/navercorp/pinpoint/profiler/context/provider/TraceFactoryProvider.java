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
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.ThreadLocalReferenceFactory;
import com.navercorp.pinpoint.profiler.context.ThreadLocalTraceFactory;
import com.navercorp.pinpoint.profiler.context.id.AsyncIdGenerator;
import com.navercorp.pinpoint.profiler.context.BaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.CallStackFactory;
import com.navercorp.pinpoint.profiler.context.DefaultBaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.id.IdGenerator;
import com.navercorp.pinpoint.profiler.context.LoggingBaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.id.TraceRootFactory;
import com.navercorp.pinpoint.profiler.context.recorder.RecorderFactory;
import com.navercorp.pinpoint.profiler.context.SpanFactory;
import com.navercorp.pinpoint.profiler.context.TraceFactory;
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

    private final ProfilerConfig profilerConfig;
    private final TraceRootFactory traceRootFactory;
    private final StorageFactory storageFactory;
    private final Sampler sampler;
    private final IdGenerator idGenerator;
    private final AsyncIdGenerator asyncIdGenerator;

    private final ActiveTraceRepository activeTraceRepository;

    private final CallStackFactory callStackFactory;

    private final SpanFactory spanFactory;
    private final RecorderFactory recorderFactory;


    @Inject
    public TraceFactoryProvider(ProfilerConfig profilerConfig, TraceRootFactory traceRootFactory, CallStackFactory callStackFactory, StorageFactory storageFactory, Sampler sampler, IdGenerator idGenerator, AsyncIdGenerator asyncIdGenerator,
                                Provider<ActiveTraceRepository> activeTraceRepositoryProvider, SpanFactory spanFactory, RecorderFactory recorderFactory) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
        this.traceRootFactory = Assert.requireNonNull(traceRootFactory, "traceRootFactory must not be null");
        this.callStackFactory = Assert.requireNonNull(callStackFactory, "callStackFactory must not be null");
        this.storageFactory = Assert.requireNonNull(storageFactory, "storageFactory must not be null");
        this.sampler = Assert.requireNonNull(sampler, "sampler must not be null");
        this.idGenerator = Assert.requireNonNull(idGenerator, "idGenerator must not be null");
        this.asyncIdGenerator = Assert.requireNonNull(asyncIdGenerator, "asyncIdGenerator must not be null");


        Assert.requireNonNull(activeTraceRepositoryProvider, "activeTraceRepositoryProvider must not be null");
        this.activeTraceRepository = activeTraceRepositoryProvider.get();

        this.spanFactory = Assert.requireNonNull(spanFactory, "spanFactory must not be null");
        this.recorderFactory = Assert.requireNonNull(recorderFactory, "recorderFactory must not be null");

    }

    @Override
    public TraceFactory get() {

        BaseTraceFactory baseTraceFactory = new DefaultBaseTraceFactory(traceRootFactory, callStackFactory, storageFactory, sampler, idGenerator,
                asyncIdGenerator, spanFactory, recorderFactory);
        if (isDebugEnabled()) {
            baseTraceFactory = LoggingBaseTraceFactory.wrap(baseTraceFactory);
        }

        TraceFactory traceFactory = newTraceFactory(baseTraceFactory);
        if (this.activeTraceRepository != null) {
            this.logger.debug("enable ActiveTrace");
            traceFactory = ActiveTraceFactory.wrap(traceFactory, this.activeTraceRepository);
        }

        return traceFactory;
    }

    private TraceFactory newTraceFactory(BaseTraceFactory baseTraceFactory) {
        final String threadLocalFactoryVersion = profilerConfig.readString("profiler.threadlocalfactory.version", "V2");
        logger.info("ThreadLocalFactory version:{}", threadLocalFactoryVersion);
        if ("V1".equalsIgnoreCase(threadLocalFactoryVersion)) {
            // V1
            return new ThreadLocalTraceFactory(baseTraceFactory);
        }
        // V2
        return new ThreadLocalReferenceFactory(baseTraceFactory);
    }

    private boolean isDebugEnabled() {
        final Logger logger = LoggerFactory.getLogger(DefaultBaseTraceFactory.class);
        return logger.isDebugEnabled();
    }

}
