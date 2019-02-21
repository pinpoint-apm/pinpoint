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

/**
 * @author Woonduk Kang(emeroad)
 */
public class RpcModuleLifeCycle implements ModuleLifeCycle {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final Provider<CommandDispatcher> commandDispatcherProvider;
    private final Provider<PinpointClientFactory> clientFactoryProvider;
    private final Provider<EnhancedDataSender<Object>> tcpDataSenderProvider;

    private final Provider<PinpointClientFactory> spanStatClientFactoryProvider;
    private final Provider<DataSender> spanDataSenderProvider;
    private final Provider<DataSender> statDataSenderProvider;

    private CommandDispatcher commandDispatcher;

    private PinpointClientFactory clientFactory;
    private EnhancedDataSender<Object> tcpDataSender;

    private PinpointClientFactory spanStatClientFactory;
    private DataSender spanDataSender;
    private DataSender statDataSender;

    @Inject
    public RpcModuleLifeCycle(
            Provider<CommandDispatcher> commandDispatcherProvider,
            @DefaultClientFactory Provider<PinpointClientFactory> clientFactoryProvider,
            Provider<EnhancedDataSender<Object>> tcpDataSenderProvider,
            @SpanStatClientFactory Provider<PinpointClientFactory> spanStatClientFactoryProvider,
            @SpanDataSender Provider<DataSender> spanDataSenderProvider,
            @StatDataSender Provider<DataSender> statDataSenderProvider
            ) {
        this.commandDispatcherProvider = Assert.requireNonNull(commandDispatcherProvider, "commandDispatcherProvider must not be null");
        this.clientFactoryProvider = Assert.requireNonNull(clientFactoryProvider, "clientFactoryProvider must not be null");
        this.tcpDataSenderProvider = Assert.requireNonNull(tcpDataSenderProvider, "tcpDataSenderProvider must not be null");

        this.spanStatClientFactoryProvider = Assert.requireNonNull(spanStatClientFactoryProvider, "spanStatClientFactoryProvider must not be null");
        this.spanDataSenderProvider = Assert.requireNonNull(spanDataSenderProvider, "spanDataSenderProvider must not be null");
        this.statDataSenderProvider = Assert.requireNonNull(statDataSenderProvider, "statDataSenderProvider must not be null");
    }

    @Override
    public void start() {
        logger.info("start()");
        this.commandDispatcher = this.commandDispatcherProvider.get();
        logger.info("commandDispatcher:{}", commandDispatcher);

        this.clientFactory = clientFactoryProvider.get();
        logger.info("pinpointClientFactory:{}", clientFactory);

        this.tcpDataSender = tcpDataSenderProvider.get();
        logger.info("tcpDataSenderProvider:{}", tcpDataSender);


        this.spanStatClientFactory = spanStatClientFactoryProvider.get();
        logger.info("spanStatClientFactory:{}", spanStatClientFactory);

        this.spanDataSender = spanDataSenderProvider.get();
        logger.info("spanDataSenderProvider:{}", spanDataSender);

        this.statDataSender = this.statDataSenderProvider.get();
        logger.info("statDataSenderProvider:{}", statDataSender);

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
        if (spanStatClientFactory != null) {
            this.spanStatClientFactory.release();
        }

        if (tcpDataSender != null) {
            this.tcpDataSender.stop();
        }
        if (clientFactory != null) {
            clientFactory.release();
        }

        if (commandDispatcher != null) {
            this.commandDispatcher.close();
        }
    }

    @Override
    public String toString() {
        return "RpcModuleLifeCycle{" +
                ", commandDispatcherProvider=" + commandDispatcherProvider +
                ", clientFactoryProvider=" + clientFactoryProvider +
                ", tcpDataSenderProvider=" + tcpDataSenderProvider +
                ", spanStatClientFactoryProvider=" + spanStatClientFactoryProvider +
                ", spanDataSenderProvider=" + spanDataSenderProvider +
                ", statDataSenderProvider=" + statDataSenderProvider +
                '}';
    }
}
