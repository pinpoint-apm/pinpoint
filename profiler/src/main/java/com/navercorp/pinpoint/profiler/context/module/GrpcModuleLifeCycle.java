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
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;

import java.util.concurrent.ExecutorService;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcModuleLifeCycle implements ModuleLifeCycle {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final Provider<EnhancedDataSender<Object>> tcpDataSenderProvider;
    private final Provider<DataSender> spanDataSenderProvider;
    private final Provider<DataSender> statDataSenderProvider;

    private final Provider<ExecutorService> dnsExecutorServiceProvider;

    private EnhancedDataSender<Object> tcpDataSender;

    private DataSender spanDataSender;
    private DataSender statDataSender;

    private ExecutorService dnsExecutorService;

    @Inject
    public GrpcModuleLifeCycle(
            Provider<EnhancedDataSender<Object>> tcpDataSenderProvider,
            @SpanDataSender Provider<DataSender> spanDataSenderProvider,
            @StatDataSender Provider<DataSender> statDataSenderProvider,
            Provider<ExecutorService> dnsExecutorServiceProvider
            ) {
        this.tcpDataSenderProvider = Assert.requireNonNull(tcpDataSenderProvider, "tcpDataSenderProvider must not be null");
        this.spanDataSenderProvider = Assert.requireNonNull(spanDataSenderProvider, "spanDataSenderProvider must not be null");
        this.statDataSenderProvider = Assert.requireNonNull(statDataSenderProvider, "statDataSenderProvider must not be null");
        this.dnsExecutorServiceProvider = Assert.requireNonNull(dnsExecutorServiceProvider, "dnsExecutorServiceProvider must not be null");
    }

    @Override
    public void start() {
        logger.info("start()");

        this.tcpDataSender = tcpDataSenderProvider.get();
        logger.info("tcpDataSenderProvider:{}", tcpDataSender);

        this.spanDataSender = spanDataSenderProvider.get();
        logger.info("spanDataSenderProvider:{}", spanDataSender);

        this.statDataSender = this.statDataSenderProvider.get();
        logger.info("statDataSenderProvider:{}", statDataSender);

        this.dnsExecutorService = dnsExecutorServiceProvider.get();

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

        if (tcpDataSender != null) {
            this.tcpDataSender.stop();
        }

        if (dnsExecutorService != null) {
            this.dnsExecutorService.shutdown();
        }
    }

    @Override
    public String toString() {
        return "GrpcModuleLifeCycle{" +
                ", tcpDataSenderProvider=" + tcpDataSenderProvider +
                ", spanDataSenderProvider=" + spanDataSenderProvider +
                ", statDataSenderProvider=" + statDataSenderProvider +
                '}';
    }
}
