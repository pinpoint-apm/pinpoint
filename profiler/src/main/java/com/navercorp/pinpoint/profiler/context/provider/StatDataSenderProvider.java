/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.context.module.SpanStatClientFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.profiler.sender.UdpDataSenderFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author Taejin Koo
 */
public class StatDataSenderProvider implements Provider<DataSender> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String UDP_EXECUTOR_NAME = "Pinpoint-UdpStatDataExecutor";

    private final Provider<PinpointClientFactory> clientFactoryProvider;

    private final String ip;
    private final int port;
    private final int writeQueueSize;
    private final int timeout;
    private final int sendBufferSize;
    private final String ioType;
    private final String transportType;

    @Inject
    public StatDataSenderProvider(ProfilerConfig profilerConfig, @SpanStatClientFactory Provider<PinpointClientFactory> clientFactoryProvider) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (clientFactoryProvider == null) {
            throw new NullPointerException("clientFactoryProvider must not be null");
        }

        this.clientFactoryProvider = clientFactoryProvider;

        this.ip = profilerConfig.getCollectorStatServerIp();
        this.port = profilerConfig.getCollectorStatServerPort();
        this.writeQueueSize = profilerConfig.getStatDataSenderWriteQueueSize();
        this.timeout = profilerConfig.getStatDataSenderSocketTimeout();
        this.sendBufferSize = profilerConfig.getStatDataSenderSocketSendBufferSize();
        this.ioType = profilerConfig.getStatDataSenderSocketType();
        this.transportType = profilerConfig.getStatDataSenderTransportType();
    }

    @Override
    public DataSender get() {
        if ("TCP".equalsIgnoreCase(transportType)) {
            if ("OIO".equalsIgnoreCase(ioType)) {
                logger.warn("TCP transport not support OIO type.(only support NIO)");
            }

            PinpointClientFactory pinpointClientFactory = clientFactoryProvider.get();
            InetSocketAddress address = new InetSocketAddress(ip, port);
            return new TcpDataSender("StatDataSender", address, pinpointClientFactory);
        } else {
            UdpDataSenderFactory factory = new UdpDataSenderFactory(ip, port, UDP_EXECUTOR_NAME, writeQueueSize, timeout, sendBufferSize);
            return factory.create(ioType);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StatDataSenderProvider{");
        sb.append("ip='").append(ip).append('\'');
        sb.append(", port=").append(port);
        sb.append(", writeQueueSize=").append(writeQueueSize);
        sb.append(", timeout=").append(timeout);
        sb.append(", sendBufferSize=").append(sendBufferSize);
        sb.append(", ioType='").append(ioType).append('\'');
        sb.append(", transportType='").append(transportType).append('\'');
        sb.append('}');
        return sb.toString();
    }

}

