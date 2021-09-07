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

package com.navercorp.pinpoint.profiler.context.provider.thrift;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.context.SpanType;
import com.navercorp.pinpoint.profiler.context.module.SpanDataSender;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.context.thrift.config.ThriftTransportConfig;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.MessageSerializer;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.profiler.sender.ThriftMessageSerializer;
import com.navercorp.pinpoint.profiler.sender.UdpDataSenderFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class SpanDataSenderProvider  implements Provider<DataSender<SpanType>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String UDP_EXECUTOR_NAME = "Pinpoint-UdpSpanDataExecutor";

    private final Provider<PinpointClientFactory> clientFactoryProvider;

    private final String ip;
    private final int port;
    private final int writeQueueSize;
    private final int timeout;
    private final int sendBufferSize;
    private final String ioType;
    private final String transportType;
    private final MessageConverter<SpanType, TBase<?, ?>> messageConverter;

    @Inject
    public SpanDataSenderProvider(ThriftTransportConfig thriftTransportConfig,
                                  @SpanDataSender Provider<PinpointClientFactory> clientFactoryProvider,
                                  @SpanDataSender MessageConverter<SpanType, TBase<?, ?>> messageConverter) {
        Objects.requireNonNull(thriftTransportConfig, "thriftTransportConfig");
        this.clientFactoryProvider = Objects.requireNonNull(clientFactoryProvider, "clientFactoryProvider");

        this.ip = thriftTransportConfig.getCollectorSpanServerIp();
        this.port = thriftTransportConfig.getCollectorSpanServerPort();
        this.writeQueueSize = thriftTransportConfig.getSpanDataSenderWriteQueueSize();
        this.timeout = thriftTransportConfig.getSpanDataSenderSocketTimeout();
        this.sendBufferSize = thriftTransportConfig.getSpanDataSenderSocketSendBufferSize();
        this.ioType = thriftTransportConfig.getSpanDataSenderSocketType();
        this.transportType = thriftTransportConfig.getSpanDataSenderTransportType();
        this.messageConverter = Objects.requireNonNull(messageConverter, "messageConverter");
    }

    @Override
    public DataSender<SpanType> get() {
        if ("TCP".equalsIgnoreCase(transportType)) {
            if ("OIO".equalsIgnoreCase(ioType)) {
                logger.warn("TCP transport not support OIO type.(only support NIO)");
            }

            PinpointClientFactory pinpointClientFactory = clientFactoryProvider.get();
            MessageSerializer<SpanType, byte[]> messageSerializer = new ThriftMessageSerializer<>(messageConverter);
            return new TcpDataSender<>("SpanDataSender", ip, port, pinpointClientFactory, messageSerializer, writeQueueSize);
        } else {
            UdpDataSenderFactory<SpanType> factory = new UdpDataSenderFactory<>(ip, port, UDP_EXECUTOR_NAME, writeQueueSize, timeout, sendBufferSize, messageConverter);
            return factory.create(ioType);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpanDataSenderProvider{");
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

