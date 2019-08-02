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
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.BaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.CallStackFactory;
import com.navercorp.pinpoint.profiler.context.DefaultBaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.LoggingBaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.SpanFactory;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.id.TraceRootFactory;
import com.navercorp.pinpoint.profiler.context.recorder.RecorderFactory;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Inject
    public BaseTraceFactoryProvider(TraceRootFactory traceRootFactory, StorageFactory storageFactory,
                                    TraceSampler traceSampler,
                                    CallStackFactory<SpanEvent> callStackFactory, SpanFactory spanFactory, RecorderFactory recorderFactory, ActiveTraceRepository activeTraceRepository) {
        this.traceRootFactory = Assert.requireNonNull(traceRootFactory, "traceRootFactory");

        this.callStackFactory = Assert.requireNonNull(callStackFactory, "callStackFactory");
        this.storageFactory = Assert.requireNonNull(storageFactory, "storageFactory");
        this.traceSampler = Assert.requireNonNull(traceSampler, "traceSampler");

        this.spanFactory = Assert.requireNonNull(spanFactory, "spanFactory");
        this.recorderFactory = Assert.requireNonNull(recorderFactory, "recorderFactory");
        this.activeTraceRepository = Assert.requireNonNull(activeTraceRepository, "activeTraceRepository");

    }

    @Override
    public BaseTraceFactory get() {
        BaseTraceFactory baseTraceFactory = new DefaultBaseTraceFactory(traceRootFactory, callStackFactory, storageFactory, traceSampler,
                spanFactory, recorderFactory, activeTraceRepository);
        if (isDebugEnabled()) {
            baseTraceFactory = LoggingBaseTraceFactory.wrap(baseTraceFactory);
        }
        return baseTraceFactory;
    }


    private boolean isDebugEnabled() {
        final Logger logger = LoggerFactory.getLogger(DefaultBaseTraceFactory.class);
        return logger.isDebugEnabled();
    }


}
