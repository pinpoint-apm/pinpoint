/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.stat.activethread;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.active.EmptyActiveTraceRepository;
import com.navercorp.pinpoint.profiler.monitor.metric.activethread.ActiveTraceMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.activethread.DefaultActiveTraceMetric;

/**
 * @author HyunGil Jeong
 */
public class ActiveTraceMetricProvider implements Provider<ActiveTraceMetric> {

    private final ActiveTraceRepository activeTraceRepository;

    @Inject
    public ActiveTraceMetricProvider(Provider<ActiveTraceRepository> activeTraceRepositoryProvider) {
        if (activeTraceRepositoryProvider == null) {
            throw new NullPointerException("activeTraceRepositoryProvider must not be null");
        }
        this.activeTraceRepository = activeTraceRepositoryProvider.get();
    }

    @Override
    public ActiveTraceMetric get() {
        if (activeTraceRepository instanceof EmptyActiveTraceRepository) {
            return ActiveTraceMetric.UNSUPPORTED_ACTIVE_TRACE_METRIC;
        } else {
            return new DefaultActiveTraceMetric(activeTraceRepository);
        }
    }
}
