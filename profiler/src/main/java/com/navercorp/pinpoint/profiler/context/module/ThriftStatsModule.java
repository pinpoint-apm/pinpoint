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
import com.navercorp.pinpoint.thrift.dto.TActiveTrace;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TCpuLoad;
import com.navercorp.pinpoint.thrift.dto.TDataSourceList;
import com.navercorp.pinpoint.thrift.dto.TDeadlock;
import com.navercorp.pinpoint.thrift.dto.TDirectBuffer;
import com.navercorp.pinpoint.thrift.dto.TFileDescriptor;
import com.navercorp.pinpoint.thrift.dto.TJvmGc;
import com.navercorp.pinpoint.thrift.dto.TResponseTime;
import com.navercorp.pinpoint.thrift.dto.TTransaction;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ThriftStatsModule extends AbstractModule {

    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        binder().requireAtInjectOnConstructors();
        binder().disableCircularProxies();

        // gc
        TypeLiteral<AgentStatMetricCollector<TJvmGc>> jvmGcCollector = new TypeLiteral<AgentStatMetricCollector<TJvmGc>>() {};
        bind(jvmGcCollector).toProvider(JvmGcMetricCollectorProvider.class).in(Scopes.SINGLETON);

        // cpu
        TypeLiteral<AgentStatMetricCollector<TCpuLoad>> cpuLoadCollector = new TypeLiteral<AgentStatMetricCollector<TCpuLoad>>() {};
        bind(cpuLoadCollector).toProvider(CpuLoadMetricCollectorProvider.class).in(Scopes.SINGLETON);

        // FD
        TypeLiteral<AgentStatMetricCollector<TFileDescriptor>> fdCollector = new TypeLiteral<AgentStatMetricCollector<TFileDescriptor>>() {};
        bind(fdCollector).toProvider(FileDescriptorMetricCollectorProvider.class).in(Scopes.SINGLETON);

        // buffer
        TypeLiteral<AgentStatMetricCollector<TDirectBuffer>> bufferCollector = new TypeLiteral<AgentStatMetricCollector<TDirectBuffer>>() {};
        bind(bufferCollector).toProvider(BufferMetricCollectorProvider.class).in(Scopes.SINGLETON);

        // transaction
        TypeLiteral<AgentStatMetricCollector<TTransaction>> transactionCollector = new TypeLiteral<AgentStatMetricCollector<TTransaction>>() {};
        bind(transactionCollector).toProvider(TransactionMetricCollectorProvider.class).in(Scopes.SINGLETON);

        // activeTrace
        TypeLiteral<AgentStatMetricCollector<TActiveTrace>> activeTraceCollector = new TypeLiteral<AgentStatMetricCollector<TActiveTrace>>() {};
        bind(activeTraceCollector).toProvider(ActiveTraceMetricCollectorProvider.class).in(Scopes.SINGLETON);

        // responseTime
        TypeLiteral<AgentStatMetricCollector<TResponseTime>> responseTimeCollector = new TypeLiteral<AgentStatMetricCollector<TResponseTime>>() {};
        bind(responseTimeCollector).toProvider(ResponseTimeMetricCollectorProvider.class).in(Scopes.SINGLETON);

        // datasource
        TypeLiteral<AgentStatMetricCollector<TDataSourceList>> datasourceCollector = new TypeLiteral<AgentStatMetricCollector<TDataSourceList>>() {};
        bind(datasourceCollector).toProvider(DataSourceMetricCollectorProvider.class).in(Scopes.SINGLETON);

        // deadlock
        TypeLiteral<AgentStatMetricCollector<TDeadlock>> deadlockCollector = new TypeLiteral<AgentStatMetricCollector<TDeadlock>>() {};
        bind(deadlockCollector).toProvider(DeadlockMetricCollectorProvider.class).in(Scopes.SINGLETON);

        // stat
        TypeLiteral<AgentStatMetricCollector<TAgentStat>> statMetric = new TypeLiteral<AgentStatMetricCollector<TAgentStat>>() {};
        bind(statMetric).annotatedWith(Names.named("AgentStatCollector"))
                .to(AgentStatCollector.class).in(Scopes.SINGLETON);
    }

}