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

package com.navercorp.pinpoint.profiler.context.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.navercorp.pinpoint.profiler.context.provider.stat.activethread.ActiveTraceMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.activethread.ActiveTraceMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.cpu.CpuLoadMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.cpu.CpuLoadMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.datasource.DataSourceMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.datasource.DataSourceMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.deadlock.DeadlockMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.deadlock.DeadlockMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.directbuffer.DirectBufferMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.directbuffer.DirectBufferMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.filedescriptor.FileDescriptorMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.filedescriptor.FileDescriptorMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc.DetailedGarbageCollectorMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc.DetailedMemoryMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc.GarbageCollectorMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc.JvmGcMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc.MemoryMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.response.ResponseTimeMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.response.ResponseTimeMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.transaction.TransactionMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.transaction.TransactionMetricProvider;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.activethread.ActiveTraceMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.cpu.CpuLoadMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.datasource.DataSourceMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.deadlock.DeadlockMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.directbuffer.DirectBufferMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.filedescriptor.FileDescriptorMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.jvmgc.JvmGcMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.response.ResponseTimeMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.transaction.TransactionMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.activethread.ActiveTraceMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSourceMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.DeadlockMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.directbuffer.DirectBufferMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.filedescriptor.FileDescriptorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.DetailedGarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.GarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.DetailedMemoryMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.MemoryMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.transaction.TransactionMetric;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;

/**
 * @author Woonduk Kang(emeroad)
 */
public class StatsModule extends AbstractModule {

    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        binder().requireAtInjectOnConstructors();
        binder().disableCircularProxies();


        bind(MemoryMetric.class).toProvider(MemoryMetricProvider.class).in(Scopes.SINGLETON);
        bind(DetailedMemoryMetric.class).toProvider(DetailedMemoryMetricProvider.class).in(Scopes.SINGLETON);
        bind(GarbageCollectorMetric.class).toProvider(GarbageCollectorMetricProvider.class).in(Scopes.SINGLETON);
        bind(DetailedGarbageCollectorMetric.class).toProvider(DetailedGarbageCollectorMetricProvider.class).in(Scopes.SINGLETON);
        bind(JvmGcMetricCollector.class).toProvider(JvmGcMetricCollectorProvider.class).in(Scopes.SINGLETON);

        bind(CpuLoadMetric.class).toProvider(CpuLoadMetricProvider.class).in(Scopes.SINGLETON);
        bind(CpuLoadMetricCollector.class).toProvider(CpuLoadMetricCollectorProvider.class).in(Scopes.SINGLETON);

        bind(FileDescriptorMetric.class).toProvider(FileDescriptorMetricProvider.class).in(Scopes.SINGLETON);
        bind(FileDescriptorMetricCollector.class).toProvider(FileDescriptorMetricCollectorProvider.class).in(Scopes.SINGLETON);

        bind(DirectBufferMetric.class).toProvider(DirectBufferMetricProvider.class).in(Scopes.SINGLETON);
        bind(DirectBufferMetricCollector.class).toProvider(DirectBufferMetricCollectorProvider.class).in(Scopes.SINGLETON);

        bind(TransactionMetric.class).toProvider(TransactionMetricProvider.class).in(Scopes.SINGLETON);
        bind(TransactionMetricCollector.class).toProvider(TransactionMetricCollectorProvider.class).in(Scopes.SINGLETON);

        bind(ActiveTraceMetric.class).toProvider(ActiveTraceMetricProvider.class).in(Scopes.SINGLETON);
        bind(ActiveTraceMetricCollector.class).toProvider(ActiveTraceMetricCollectorProvider.class).in(Scopes.SINGLETON);

        bind(ResponseTimeMetric.class).toProvider(ResponseTimeMetricProvider.class).in(Scopes.SINGLETON);
        bind(ResponseTimeMetricCollector.class).toProvider(ResponseTimeMetricCollectorProvider.class).in(Scopes.SINGLETON);

        bind(DataSourceMetric.class).toProvider(DataSourceMetricProvider.class).in(Scopes.SINGLETON);
        bind(DataSourceMetricCollector.class).toProvider(DataSourceMetricCollectorProvider.class).in(Scopes.SINGLETON);

        bind(DeadlockMetric.class).toProvider(DeadlockMetricProvider.class).in(Scopes.SINGLETON);
        bind(DeadlockMetricCollector.class).toProvider(DeadlockMetricCollectorProvider.class).in(Scopes.SINGLETON);

        bind(new TypeLiteral<AgentStatMetricCollector<TAgentStat>>() {})
                .annotatedWith(Names.named("AgentStatCollector"))
                .to(AgentStatCollector.class).in(Scopes.SINGLETON);
    }
}
