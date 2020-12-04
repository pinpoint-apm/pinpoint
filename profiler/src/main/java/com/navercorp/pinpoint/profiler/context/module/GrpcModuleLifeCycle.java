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
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.ExecutorUtils;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.grpc.metric.ChannelzScheduledReporter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcModuleLifeCycle implements ModuleLifeCycle {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final Provider<EnhancedDataSender<Object>> agentDataSenderProvider;
    private final Provider<EnhancedDataSender<Object>> metadataDataSenderProvider;
    private final Provider<DataSender> spanDataSenderProvider;
    private final Provider<DataSender> statDataSenderProvider;

    private final Provider<ExecutorService> dnsExecutorServiceProvider;
    private final Provider<ScheduledExecutorService> reconnectScheduledExecutorProvider;

    private EnhancedDataSender<Object> agentDataSender;
    private EnhancedDataSender<Object> metadataDataSender;

    private DataSender spanDataSender;
    private DataSender statDataSender;

    private ExecutorService dnsExecutorService;
    private ScheduledExecutorService reconnectScheduledExecutorService;

    private final ChannelzScheduledReporter reporter;

    @Inject
    public GrpcModuleLifeCycle(
            @AgentDataSender Provider<EnhancedDataSender<Object>> agentDataSenderProvider,
            @MetadataDataSender Provider<EnhancedDataSender<Object>> metadataDataSenderProvider,
            @SpanDataSender Provider<DataSender> spanDataSenderProvider,
            @StatDataSender Provider<DataSender> statDataSenderProvider,
            Provider<ExecutorService> dnsExecutorServiceProvider,
            Provider<ScheduledExecutorService> reconnectScheduledExecutorProvider,
            ChannelzScheduledReporter reporter
    ) {
        this.agentDataSenderProvider = Assert.requireNonNull(agentDataSenderProvider, "agentDataSenderProvider");
        this.metadataDataSenderProvider = Assert.requireNonNull(metadataDataSenderProvider, "metadataDataSenderProvider");
        this.spanDataSenderProvider = Assert.requireNonNull(spanDataSenderProvider, "spanDataSenderProvider");
        this.statDataSenderProvider = Assert.requireNonNull(statDataSenderProvider, "statDataSenderProvider");
        this.dnsExecutorServiceProvider = Assert.requireNonNull(dnsExecutorServiceProvider, "dnsExecutorServiceProvider");
        this.reconnectScheduledExecutorProvider = Assert.requireNonNull(reconnectScheduledExecutorProvider, "reconnectScheduledExecutorProvider");
        this.reporter = Assert.requireNonNull(reporter, "reporter");
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
        if (spanDataSender != null) {
            this.spanDataSender.stop();
        }
        if (statDataSender != null) {
            this.statDataSender.stop();
        }

        if (agentDataSender != null) {
            this.agentDataSender.stop();
        }

        if (metadataDataSender != null) {
            this.metadataDataSender.stop();
        }

        if (dnsExecutorService != null) {
            ExecutorUtils.shutdownExecutorService("dnsExecutor", dnsExecutorService);
        }
        if (reconnectScheduledExecutorService != null) {
            ExecutorUtils.shutdownExecutorService("reconnectScheduledExecutor", reconnectScheduledExecutorService);
        }
        if (reporter != null) {
            reporter.stop();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GrpcModuleLifeCycle{");
        sb.append(", agentDataSender=").append(agentDataSender);
        sb.append(", metadataDataSender=").append(metadataDataSender);
        sb.append(", spanDataSender=").append(spanDataSender);
        sb.append(", statDataSender=").append(statDataSender);
        sb.append(", dnsExecutorService=").append(dnsExecutorService);
        sb.append(", reconnectScheduledExecutorService=" + reconnectScheduledExecutorService);
        sb.append('}');
        return sb.toString();
    }
}
