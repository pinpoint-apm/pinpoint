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


import com.google.inject.Provider;

import com.google.inject.Inject;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.active.DefaultActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.active.EmptyActiveTraceRepository;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeCollector;


/**
 * @author Woonduk Kang(emeroad)
 */
public class ActiveTraceRepositoryProvider implements Provider<ActiveTraceRepository> {

    private final ProfilerConfig profilerConfig;
    private final ResponseTimeCollector responseTimeCollector;

    @Inject
    public ActiveTraceRepositoryProvider(ProfilerConfig profilerConfig, ResponseTimeCollector responseTimeCollector) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
        this.responseTimeCollector = Assert.requireNonNull(responseTimeCollector, "responseTimeCollector must not be null");

    }

    public ActiveTraceRepository get() {
        if (profilerConfig.isTraceAgentActiveThread()) {
            return new DefaultActiveTraceRepository(responseTimeCollector);
        }
        ActiveTraceRepository emptyActiveTraceRepository = new EmptyActiveTraceRepository(responseTimeCollector);
        return emptyActiveTraceRepository;
    }

}
