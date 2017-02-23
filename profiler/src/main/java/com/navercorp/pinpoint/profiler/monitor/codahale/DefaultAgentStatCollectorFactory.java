/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.codahale;

import com.google.inject.Inject;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.context.TransactionCounter;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.monitor.DataSourceMonitorWrapper;
import com.navercorp.pinpoint.profiler.context.monitor.DatabaseInfoCache;
import com.navercorp.pinpoint.profiler.context.monitor.DefaultPluginMonitorContext;
import com.navercorp.pinpoint.profiler.context.monitor.PluginMonitorContext;
import com.navercorp.pinpoint.profiler.context.monitor.PluginMonitorWrapperLocator;
import com.navercorp.pinpoint.profiler.monitor.MonitorName;
import com.navercorp.pinpoint.profiler.monitor.codahale.activetrace.ActiveTraceMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.activetrace.DefaultActiveTraceMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.activetrace.metric.ActiveTraceMetricSet;
import com.navercorp.pinpoint.profiler.monitor.codahale.cpu.CpuLoadCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.cpu.DefaultCpuLoadCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.cpu.metric.CpuLoadMetricSet;
import com.navercorp.pinpoint.profiler.monitor.codahale.datasource.DataSourceCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.datasource.DefaultDataSourceCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.datasource.metric.DataSourceMetricSet;
import com.navercorp.pinpoint.profiler.monitor.codahale.gc.CmsCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.gc.CmsDetailedMetricsCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.gc.G1Collector;
import com.navercorp.pinpoint.profiler.monitor.codahale.gc.G1DetailedMetricsCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.gc.GarbageCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.gc.ParallelCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.gc.ParallelDetailedMetricsCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.gc.SerialCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.gc.SerialDetailedMetricsCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.gc.UnknownGarbageCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.tps.DefaultTransactionMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.tps.TransactionMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.tps.metric.TransactionMetricSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues.*;

/**
 * @author emeroad
 * @author harebox
 * @author hyungil.jeong
 */
public class DefaultAgentStatCollectorFactory implements AgentStatCollectorFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MetricMonitorRegistry monitorRegistry;
    private final GarbageCollector garbageCollector;
    private final CpuLoadCollector cpuLoadCollector;
    private final TransactionMetricCollector transactionMetricCollector;
    private final ActiveTraceMetricCollector activeTraceMetricCollector;
    private final DataSourceCollector dataSourceCollector;

    @Inject
    public DefaultAgentStatCollectorFactory(ProfilerConfig profilerConfig, ActiveTraceRepository activeTraceRepository, TransactionCounter transactionCounter, PluginMonitorContext pluginMonitorContext, DatabaseInfoCache databaseInfoCache) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
//        if (activeTraceRepository == null) {
//            throw new NullPointerException("activeTraceRepository must not be null");
//        }
        if (transactionCounter == null) {
            throw new NullPointerException("transactionCounter must not be null");
        }
//        if (pluginMonitorContext == null) {
//            throw new NullPointerException("pluginMonitorContext must not be null");
//        }
        this.monitorRegistry = createRegistry();
        this.garbageCollector = createGarbageCollector(profilerConfig.isProfilerJvmCollectDetailedMetrics());
        this.cpuLoadCollector = createCpuLoadCollector(profilerConfig.getProfilerJvmVendorName());
        this.transactionMetricCollector = createTransactionMetricCollector(transactionCounter);
        this.activeTraceMetricCollector = createActiveTraceCollector(activeTraceRepository, profilerConfig.isTraceAgentActiveThread());
        this.dataSourceCollector = createDataSourceCollector(pluginMonitorContext, databaseInfoCache);
    }

    private MetricMonitorRegistry createRegistry() {
        final MetricMonitorRegistry monitorRegistry = new MetricMonitorRegistry();
        return monitorRegistry;
    }

    /**
     * create with garbage collector types based on metric registry keys
     */
    private GarbageCollector createGarbageCollector(boolean collectDetailedMetrics) {
        MetricMonitorRegistry registry = this.monitorRegistry;
        registry.registerJvmMemoryMonitor(new MonitorName(MetricMonitorValues.JVM_MEMORY));
        registry.registerJvmGcMonitor(new MonitorName(MetricMonitorValues.JVM_GC));

        Collection<String> keys = registry.getRegistry().getNames();
        GarbageCollector garbageCollectorToReturn = new UnknownGarbageCollector();

        if (collectDetailedMetrics) {
            if (keys.contains(JVM_GC_SERIAL_OLDGEN_COUNT)) {
                garbageCollectorToReturn = new SerialDetailedMetricsCollector(registry);
            } else if (keys.contains(JVM_GC_PS_OLDGEN_COUNT)) {
                garbageCollectorToReturn = new ParallelDetailedMetricsCollector(registry);
            } else if (keys.contains(JVM_GC_CMS_OLDGEN_COUNT)) {
                garbageCollectorToReturn = new CmsDetailedMetricsCollector(registry);
            } else if (keys.contains(JVM_GC_G1_OLDGEN_COUNT)) {
                garbageCollectorToReturn = new G1DetailedMetricsCollector(registry);
            }
        } else {
            if (keys.contains(JVM_GC_SERIAL_OLDGEN_COUNT)) {
                garbageCollectorToReturn = new SerialCollector(registry);
            } else if (keys.contains(JVM_GC_PS_OLDGEN_COUNT)) {
                garbageCollectorToReturn = new ParallelCollector(registry);
            } else if (keys.contains(JVM_GC_CMS_OLDGEN_COUNT)) {
                garbageCollectorToReturn = new CmsCollector(registry);
            } else if (keys.contains(JVM_GC_G1_OLDGEN_COUNT)) {
                garbageCollectorToReturn = new G1Collector(registry);
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("found : {}", garbageCollectorToReturn);
        }
        return garbageCollectorToReturn;
    }

    private CpuLoadCollector createCpuLoadCollector(String vendorName) {
        CpuLoadMetricSet cpuLoadMetricSet = this.monitorRegistry.registerCpuLoadMonitor(new MonitorName(MetricMonitorValues.CPU_LOAD), vendorName);
        if (logger.isInfoEnabled()) {
            logger.info("loaded : {}", cpuLoadMetricSet);
        }
        return new DefaultCpuLoadCollector(cpuLoadMetricSet);
    }

    private TransactionMetricCollector createTransactionMetricCollector(TransactionCounter transactionCounter) {
        if (transactionCounter == null) {
            return TransactionMetricCollector.EMPTY_TRANSACTION_METRIC_COLLECTOR;
        }

        MonitorName monitorName = new MonitorName(MetricMonitorValues.TRANSACTION);
        TransactionMetricSet transactionMetricSet = this.monitorRegistry.registerTpsMonitor(monitorName, transactionCounter);
        if (logger.isInfoEnabled()) {
            logger.info("loaded : {}", transactionMetricSet);
        }
        return new DefaultTransactionMetricCollector(transactionMetricSet);

    }

    private ActiveTraceMetricCollector createActiveTraceCollector(ActiveTraceRepository activeTraceRepository, boolean isTraceAgentActiveThread) {
        if (!isTraceAgentActiveThread) {
            return ActiveTraceMetricCollector.EMPTY_ACTIVE_TRACE_COLLECTOR;
        }

        if (activeTraceRepository != null) {
            ActiveTraceMetricSet activeTraceMetricSet = this.monitorRegistry.registerActiveTraceMetricSet(new MonitorName(MetricMonitorValues.ACTIVE_TRACE), activeTraceRepository);
            if (logger.isInfoEnabled()) {
                logger.info("loaded : {}", activeTraceMetricSet);
            }
            return new DefaultActiveTraceMetricCollector(activeTraceMetricSet);
        } else {
            logger.warn("agent set to trace active threads but no ActiveTraceLocator found");
        }
        return ActiveTraceMetricCollector.EMPTY_ACTIVE_TRACE_COLLECTOR;
    }

    private DataSourceCollector createDataSourceCollector(PluginMonitorContext pluginMonitorContext, DatabaseInfoCache databaseInfoCache) {
        if (pluginMonitorContext instanceof DefaultPluginMonitorContext) {
            PluginMonitorWrapperLocator<DataSourceMonitorWrapper> dataSourceMonitorLocator = ((DefaultPluginMonitorContext) pluginMonitorContext).getDataSourceMonitorLocator();
            if (dataSourceMonitorLocator != null) {
                DataSourceMetricSet dataSourceMetricSet = new DataSourceMetricSet(dataSourceMonitorLocator, databaseInfoCache);
                this.monitorRegistry.registerDataSourceMonitor(new MonitorName(MetricMonitorValues.DATASOURCE), dataSourceMetricSet);
                return new DefaultDataSourceCollector(dataSourceMetricSet);
            }
        }
        return DataSourceCollector.EMPTY_DATASOURCE_COLLECTOR;
    }

    @Override
    public GarbageCollector getGarbageCollector() {
        return this.garbageCollector;
    }

    @Override
    public CpuLoadCollector getCpuLoadCollector() {
        return this.cpuLoadCollector;
    }

    @Override
    public TransactionMetricCollector getTransactionMetricCollector() {
        return this.transactionMetricCollector;
    }

    @Override
    public ActiveTraceMetricCollector getActiveTraceMetricCollector() {
        return this.activeTraceMetricCollector;
    }

    @Override
    public DataSourceCollector getDataSourceCollector() {
        return this.dataSourceCollector;
    }

}
