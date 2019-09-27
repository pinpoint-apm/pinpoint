/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.navercorp.pinpoint.profiler.context.provider.stat.activethread.ActiveTraceMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.cpu.CpuLoadMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.datasource.DataSourceMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.deadlock.DeadlockMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.buffer.BufferMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.filedescriptor.FileDescriptorMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc.DetailedGarbageCollectorMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc.DetailedMemoryMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc.GarbageCollectorMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc.MemoryMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.response.ResponseTimeMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.transaction.TransactionMetricProvider;
import com.navercorp.pinpoint.profiler.monitor.metric.activethread.ActiveTraceMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSourceMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.DeadlockMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.buffer.BufferMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.filedescriptor.FileDescriptorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.DetailedGarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.GarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.DetailedMemoryMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.MemoryMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.transaction.TransactionMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class StatsModule extends AbstractModule {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void configure() {
        logger.info("configure {}", this.getClass().getSimpleName());

        binder().requireExplicitBindings();
        binder().requireAtInjectOnConstructors();
        binder().disableCircularProxies();

        // gc
        bind(MemoryMetric.class).toProvider(MemoryMetricProvider.class).in(Scopes.SINGLETON);
        bind(DetailedMemoryMetric.class).toProvider(DetailedMemoryMetricProvider.class).in(Scopes.SINGLETON);
        bind(GarbageCollectorMetric.class).toProvider(GarbageCollectorMetricProvider.class).in(Scopes.SINGLETON);
        bind(DetailedGarbageCollectorMetric.class).toProvider(DetailedGarbageCollectorMetricProvider.class).in(Scopes.SINGLETON);

        // cpu
        bind(CpuLoadMetric.class).toProvider(CpuLoadMetricProvider.class).in(Scopes.SINGLETON);

        // FD
        bind(FileDescriptorMetric.class).toProvider(FileDescriptorMetricProvider.class).in(Scopes.SINGLETON);

        // buffer
        bind(BufferMetric.class).toProvider(BufferMetricProvider.class).in(Scopes.SINGLETON);

        // transaction
        bind(TransactionMetric.class).toProvider(TransactionMetricProvider.class).in(Scopes.SINGLETON);

        // activeTrace
        bind(ActiveTraceMetric.class).toProvider(ActiveTraceMetricProvider.class).in(Scopes.SINGLETON);

        // responseTime
        bind(ResponseTimeMetric.class).toProvider(ResponseTimeMetricProvider.class).in(Scopes.SINGLETON);

        // datasource
        bind(DataSourceMetric.class).toProvider(DataSourceMetricProvider.class).in(Scopes.SINGLETON);

        // deadlock
        bind(DeadlockMetric.class).toProvider(DeadlockMetricProvider.class).in(Scopes.SINGLETON);

    }
}
