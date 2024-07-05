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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.profiler.Stoppable;
import com.navercorp.pinpoint.common.profiler.message.AsyncDataSender;
import com.navercorp.pinpoint.common.profiler.message.DataSender;
import com.navercorp.pinpoint.common.profiler.message.EnhancedDataSender;
import com.navercorp.pinpoint.common.profiler.message.ResultResponse;
import com.navercorp.pinpoint.grpc.ExecutorUtils;
import com.navercorp.pinpoint.profiler.context.SpanType;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricType;
import com.navercorp.pinpoint.profiler.sender.grpc.metric.ChannelzScheduledReporter;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcModuleLifeCycle implements ModuleLifeCycle {

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());

    private final Provider<AsyncDataSender<MetaDataType, ResultResponse>> agentDataSenderProvider;
    private final Provider<EnhancedDataSender<MetaDataType>> metadataDataSenderProvider;
    private final Provider<DataSender<SpanType>> spanDataSenderProvider;
    private final Provider<DataSender<MetricType>> statDataSenderProvider;

    private final Provider<ExecutorService> dnsExecutorServiceProvider;
    private final Provider<ScheduledExecutorService> reconnectScheduledExecutorProvider;

    private AsyncDataSender<MetaDataType, ResultResponse> agentDataSender;
    private EnhancedDataSender<MetaDataType> metadataDataSender;

    private DataSender<SpanType> spanDataSender;
    private DataSender<MetricType> statDataSender;

    private ExecutorService dnsExecutorService;
    private ScheduledExecutorService reconnectScheduledExecutorService;

    private final ChannelzScheduledReporter reporter;

    @Inject
    public GrpcModuleLifeCycle(
            @AgentDataSender Provider<AsyncDataSender<MetaDataType, ResultResponse>> agentDataSenderProvider,
            @MetadataDataSender Provider<EnhancedDataSender<MetaDataType>> metadataDataSenderProvider,
            @SpanDataSender Provider<DataSender<SpanType>> spanDataSenderProvider,
            @StatDataSender Provider<DataSender<MetricType>> statDataSenderProvider,
            Provider<ExecutorService> dnsExecutorServiceProvider,
            Provider<ScheduledExecutorService> reconnectScheduledExecutorProvider,
            ChannelzScheduledReporter reporter) {
        this.agentDataSenderProvider = Objects.requireNonNull(agentDataSenderProvider, "agentDataSenderProvider");
        this.metadataDataSenderProvider = Objects.requireNonNull(metadataDataSenderProvider, "metadataDataSenderProvider");
        this.spanDataSenderProvider = Objects.requireNonNull(spanDataSenderProvider, "spanDataSenderProvider");
        this.statDataSenderProvider = Objects.requireNonNull(statDataSenderProvider, "statDataSenderProvider");
        this.dnsExecutorServiceProvider = Objects.requireNonNull(dnsExecutorServiceProvider, "dnsExecutorServiceProvider");
        this.reconnectScheduledExecutorProvider = Objects.requireNonNull(reconnectScheduledExecutorProvider, "reconnectScheduledExecutorProvider");
        this.reporter = Objects.requireNonNull(reporter, "reporter");
    }

    @Override
    public void start() {
        logger.info("start()");

        this.agentDataSender = agentDataSenderProvider.get();
        logger.info("agetInfoDataSenderProvider:{}", agentDataSender);

        this.metadataDataSender = metadataDataSenderProvider.get();
        logger.info("metadataDataSenderProvider:{}", metadataDataSender);

        this.spanDataSender = spanDataSenderProvider.get();
        logger.info("spanDataSenderProvider:{}", spanDataSender);

        this.statDataSender = this.statDataSenderProvider.get();
        logger.info("statDataSenderProvider:{}", statDataSender);

        this.dnsExecutorService = dnsExecutorServiceProvider.get();
        logger.info("dnsExecutorServiceProvider:{}", dnsExecutorService);

        this.reconnectScheduledExecutorService = reconnectScheduledExecutorProvider.get();
        logger.info("reconnectScheduledExecutorServiceProvider:{}", reconnectScheduledExecutorService);

    }

    @Override
    public void shutdown() {
        logger.info("shutdown()");
        Stoppable.stopQuietly(spanDataSender);
        Stoppable.stopQuietly(statDataSender);
        Stoppable.stopQuietly(agentDataSender);
        Stoppable.stopQuietly(metadataDataSender);

        if (dnsExecutorService != null) {
            ExecutorUtils.shutdownExecutorService("dnsExecutor", dnsExecutorService);
        }
        if (reconnectScheduledExecutorService != null) {
            ExecutorUtils.shutdownExecutorService("reconnectScheduledExecutor", reconnectScheduledExecutorService);
        }
        Stoppable.stopQuietly(reporter);
    }

    @Override
    public String toString() {
        return "GrpcModuleLifeCycle{" +
                ", agentDataSender=" + agentDataSender +
                ", metadataDataSender=" + metadataDataSender +
                ", spanDataSender=" + spanDataSender +
                ", statDataSender=" + statDataSender +
                ", dnsExecutorService=" + dnsExecutorService +
                ", reconnectScheduledExecutorService=" + reconnectScheduledExecutorService +
                '}';
    }
}
