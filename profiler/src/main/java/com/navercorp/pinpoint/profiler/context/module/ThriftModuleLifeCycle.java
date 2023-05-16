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
import com.navercorp.pinpoint.profiler.context.SpanType;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricType;
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;

import java.util.Objects;
import java.util.Set;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ThriftModuleLifeCycle implements ModuleLifeCycle {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final Provider<CommandDispatcher> commandDispatcherProvider;
    private final Provider<PinpointClientFactory> clientFactoryProvider;
    private final Provider<EnhancedDataSender<MetaDataType>> tcpDataSenderProvider;

    private final Provider<Timer> spanStatConnectTimerProvider;
    private final Provider<ChannelFactory> spanStatChannelFactoryProvider;

    private final Provider<PinpointClientFactory> spanClientFactoryProvider;
    private final Provider<PinpointClientFactory> statClientFactoryProvider;

    private final Provider<DataSender<SpanType>> spanDataSenderProvider;
    private final Provider<DataSender<MetricType>> statDataSenderProvider;

    private CommandDispatcher commandDispatcher;

    private PinpointClientFactory clientFactory;
    private EnhancedDataSender<MetaDataType> tcpDataSender;

    private Timer spanStatConnectTimer;
    private ChannelFactory spanStatChannelFactory;

    private PinpointClientFactory spanClientFactory;
    private PinpointClientFactory statClientFactory;

    private DataSender<SpanType> spanDataSender;
    private DataSender<MetricType> statDataSender;

    @Inject
    public ThriftModuleLifeCycle(
            Provider<CommandDispatcher> commandDispatcherProvider,
            @DefaultClientFactory Provider<PinpointClientFactory> clientFactoryProvider,
            @AgentDataSender Provider<EnhancedDataSender<MetaDataType>> tcpDataSenderProvider,
            @SpanStatChannelFactory Provider<Timer> spanStatConnectTimerProvider,
            @SpanStatChannelFactory Provider<ChannelFactory> spanStatChannelFactoryProvider,
            @SpanDataSender Provider<PinpointClientFactory> spanClientFactoryProvider,
            @StatDataSender Provider<PinpointClientFactory> statClientFactoryProvider,
            @SpanDataSender Provider<DataSender<SpanType>> spanDataSenderProvider,
            @StatDataSender Provider<DataSender<MetricType>> statDataSenderProvider
            ) {
        this.commandDispatcherProvider = Objects.requireNonNull(commandDispatcherProvider, "commandDispatcherProvider");
        this.clientFactoryProvider = Objects.requireNonNull(clientFactoryProvider, "clientFactoryProvider");
        this.tcpDataSenderProvider = Objects.requireNonNull(tcpDataSenderProvider, "tcpDataSenderProvider");

        this.spanStatConnectTimerProvider = Objects.requireNonNull(spanStatConnectTimerProvider, "spanStatConnectTimerProvider");

        this.spanStatChannelFactoryProvider = Objects.requireNonNull(spanStatChannelFactoryProvider, "spanStatChannelFactoryProvider");

        this.spanClientFactoryProvider = Objects.requireNonNull(spanClientFactoryProvider, "spanClientFactoryProvider");
        this.statClientFactoryProvider = Objects.requireNonNull(statClientFactoryProvider, "statClientFactoryProvider");

        this.spanDataSenderProvider = Objects.requireNonNull(spanDataSenderProvider, "spanDataSenderProvider");
        this.statDataSenderProvider = Objects.requireNonNull(statDataSenderProvider, "statDataSenderProvider");
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

        this.spanStatConnectTimer = spanStatConnectTimerProvider.get();
        logger.info("spanStatConnectTimer:{}", spanStatConnectTimer);

        this.spanStatChannelFactory = spanStatChannelFactoryProvider.get();
        logger.info("spanStatChannelFactory:{}", spanStatChannelFactory);

        this.spanClientFactory = spanClientFactoryProvider.get();
        logger.info("spanClientFactory:{}", spanClientFactory);

        this.statClientFactory = statClientFactoryProvider.get();
        logger.info("statClientFactory:{}", statClientFactory);

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

        if (spanClientFactory != null) {
            this.spanClientFactory.release();
        }
        if (statClientFactory!= null) {
            this.statClientFactory.release();
        }

        if (spanStatChannelFactory != null) {
            spanStatChannelFactory.releaseExternalResources();
        }

        if (spanStatConnectTimer != null) {
            Set<Timeout> stop = spanStatConnectTimer.stop();
            if (!stop.isEmpty()) {
                logger.info("stop Timeout:{}", stop.size());
            }
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
        return "ThriftModuleLifeCycle{" +
                "commandDispatcherProvider=" + commandDispatcherProvider +
                ", clientFactoryProvider=" + clientFactoryProvider +
                ", tcpDataSenderProvider=" + tcpDataSenderProvider +
                ", spanStatConnectTimerProvider=" + spanStatConnectTimerProvider +
                ", spanStatChannelFactoryProvider=" + spanStatChannelFactoryProvider +
                ", spanClientFactoryProvider=" + spanClientFactoryProvider +
                ", statClientFactoryProvider=" + statClientFactoryProvider +
                ", spanDataSenderProvider=" + spanDataSenderProvider +
                ", statDataSenderProvider=" + statDataSenderProvider +
                '}';
    }
}
