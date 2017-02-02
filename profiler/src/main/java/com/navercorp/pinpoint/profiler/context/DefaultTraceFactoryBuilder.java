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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceFactory;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultTraceFactoryBuilder implements TraceFactoryBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final StorageFactory storageFactory;
    private final Sampler sampler;
    private final IdGenerator idGenerator;
    private final ActiveTraceRepository activeTraceRepository;

    public DefaultTraceFactoryBuilder(StorageFactory storageFactory, Sampler sampler, IdGenerator idGenerator, ActiveTraceRepository activeTraceRepository) {
        if (storageFactory == null) {
            throw new NullPointerException("storageFactory must not be null");
        }
        if (sampler == null) {
            throw new NullPointerException("sampler must not be null");
        }
        if (idGenerator == null) {
            throw new NullPointerException("idGenerator must not be null");
        }
//        if (activeTraceRepository == null) {
//            throw new NullPointerException("activeTraceRepository must not be null");
//        }

        this.storageFactory = storageFactory;
        this.sampler = sampler;
        this.idGenerator = idGenerator;
        this.activeTraceRepository = activeTraceRepository;
    }

    public TraceFactory build(TraceContext traceContext) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }

        BaseTraceFactory baseTraceFactory = new DefaultBaseTraceFactory(traceContext, storageFactory, sampler, idGenerator);
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
