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
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHistogram;
import com.navercorp.pinpoint.profiler.context.provider.stat.activethread.ActiveTraceMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.buffer.BufferMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.cpu.CpuLoadMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.datasource.DataSourceMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.deadlock.DeadlockMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.filedescriptor.FileDescriptorMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc.JvmGcMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.response.ResponseTimeMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.transaction.TransactionMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentStatMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.JvmGcMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.buffer.BufferMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSourceMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.DeadlockMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.filedescriptor.FileDescriptorMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeValue;
import com.navercorp.pinpoint.profiler.monitor.metric.transaction.TransactionMetricSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ThriftStatsModule extends AbstractModule {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    protected void configure() {
        logger.info("configure {}", this.getClass().getSimpleName());

        binder().requireExplicitBindings();
        binder().requireAtInjectOnConstructors();
        binder().disableCircularProxies();

        // gc
        TypeLiteral<AgentStatMetricCollector<JvmGcMetricSnapshot>> jvmGcCollector = new TypeLiteral<AgentStatMetricCollector<JvmGcMetricSnapshot>>() {};
        bind(jvmGcCollector).toProvider(JvmGcMetricCollectorProvider.class).in(Scopes.SINGLETON);

        // cpu
        TypeLiteral<AgentStatMetricCollector<CpuLoadMetricSnapshot>> cpuLoadCollector = new TypeLiteral<AgentStatMetricCollector<CpuLoadMetricSnapshot>>() {};
        bind(cpuLoadCollector).toProvider(CpuLoadMetricCollectorProvider.class).in(Scopes.SINGLETON);

        // FD
        TypeLiteral<AgentStatMetricCollector<FileDescriptorMetricSnapshot>> fdCollector = new TypeLiteral<AgentStatMetricCollector<FileDescriptorMetricSnapshot>>() {};
        bind(fdCollector).toProvider(FileDescriptorMetricCollectorProvider.class).in(Scopes.SINGLETON);

        // buffer
        TypeLiteral<AgentStatMetricCollector<BufferMetricSnapshot>> bufferCollector = new TypeLiteral<AgentStatMetricCollector<BufferMetricSnapshot>>() {};
        bind(bufferCollector).toProvider(BufferMetricCollectorProvider.class).in(Scopes.SINGLETON);

        // transaction
        TypeLiteral<AgentStatMetricCollector<TransactionMetricSnapshot>> transactionCollector = new TypeLiteral<AgentStatMetricCollector<TransactionMetricSnapshot>>() {};
        bind(transactionCollector).toProvider(TransactionMetricCollectorProvider.class).in(Scopes.SINGLETON);

        // activeTrace
        TypeLiteral<AgentStatMetricCollector<ActiveTraceHistogram>> activeTraceCollector = new TypeLiteral<AgentStatMetricCollector<ActiveTraceHistogram>>() {};
        bind(activeTraceCollector).toProvider(ActiveTraceMetricCollectorProvider.class).in(Scopes.SINGLETON);

        // responseTime
        TypeLiteral<AgentStatMetricCollector<ResponseTimeValue>> responseTimeCollector = new TypeLiteral<AgentStatMetricCollector<ResponseTimeValue>>() {};
        bind(responseTimeCollector).toProvider(ResponseTimeMetricCollectorProvider.class).in(Scopes.SINGLETON);

        // datasource
        TypeLiteral<AgentStatMetricCollector<DataSourceMetricSnapshot>> datasourceCollector = new TypeLiteral<AgentStatMetricCollector<DataSourceMetricSnapshot>>() {};
        bind(datasourceCollector).toProvider(DataSourceMetricCollectorProvider.class).in(Scopes.SINGLETON);

        // deadlock
        TypeLiteral<AgentStatMetricCollector<DeadlockMetricSnapshot>> deadlockCollector = new TypeLiteral<AgentStatMetricCollector<DeadlockMetricSnapshot>>() {};
        bind(deadlockCollector).toProvider(DeadlockMetricCollectorProvider.class).in(Scopes.SINGLETON);

        // stat
        TypeLiteral<AgentStatMetricCollector<AgentStatMetricSnapshot>> statMetric = new TypeLiteral<AgentStatMetricCollector<AgentStatMetricSnapshot>>() {};
        bind(statMetric).annotatedWith(Names.named("AgentStatCollector"))
                .to(AgentStatCollector.class).in(Scopes.SINGLETON);
    }

}