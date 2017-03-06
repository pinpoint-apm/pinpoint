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
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.context.AsyncIdGenerator;
import com.navercorp.pinpoint.profiler.context.AtomicIdGenerator;
import com.navercorp.pinpoint.profiler.context.BaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.DefaultBaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.LoggingBaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.ThreadLocalTraceFactory;
import com.navercorp.pinpoint.profiler.context.TraceFactory;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceFactory;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataCacheService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TraceFactoryProvider implements Provider<TraceFactory> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final StorageFactory storageFactory;
    private final Sampler sampler;
    private final AtomicIdGenerator idGenerator;
    private final AsyncIdGenerator asyncIdGenerator;

    private final ActiveTraceRepository activeTraceRepository;

    private final ProfilerConfig profilerConfig;
    private final AgentInformation agentInformation;
    private final StringMetaDataService stringMetaDataService;
    private final SqlMetaDataCacheService sqlMetaDataCacheService;

    @Inject
    public TraceFactoryProvider(ProfilerConfig profilerConfig, StorageFactory storageFactory, Sampler sampler, AtomicIdGenerator idGenerator, AsyncIdGenerator asyncIdGenerator, ActiveTraceRepository activeTraceRepository,
                                AgentInformation agentInformation, StringMetaDataService stringMetaDataService, SqlMetaDataCacheService sqlMetaDataCacheService) {

        if (storageFactory == null) {
            throw new NullPointerException("storageFactory must not be null");
        }
        if (sampler == null) {
            throw new NullPointerException("sampler must not be null");
        }
        if (idGenerator == null) {
            throw new NullPointerException("idGenerator must not be null");
        }
        if (asyncIdGenerator == null) {
            throw new NullPointerException("asyncIdGenerator must not be null");
        }
//        if (activeTraceRepository == null) {
//            throw new NullPointerException("activeTraceRepository must not be null");
//        }
        if (agentInformation == null) {
            throw new NullPointerException("agentInformation must not be null");
        }
        if (stringMetaDataService == null) {
            throw new NullPointerException("stringMetaDataService must not be null");
        }
        if (sqlMetaDataCacheService == null) {
            throw new NullPointerException("sqlMetaDataCacheService must not be null");
        }

        this.profilerConfig = profilerConfig;
        this.storageFactory = storageFactory;
        this.sampler = sampler;
        this.idGenerator = idGenerator;
        this.asyncIdGenerator = asyncIdGenerator;
        this.activeTraceRepository = activeTraceRepository;

        this.agentInformation = agentInformation;
        this.stringMetaDataService = stringMetaDataService;
        this.sqlMetaDataCacheService = sqlMetaDataCacheService;
    }

    @Override
    public TraceFactory get() {

        BaseTraceFactory baseTraceFactory = new DefaultBaseTraceFactory(profilerConfig, storageFactory, sampler, idGenerator,
                asyncIdGenerator, agentInformation, stringMetaDataService, sqlMetaDataCacheService);
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
