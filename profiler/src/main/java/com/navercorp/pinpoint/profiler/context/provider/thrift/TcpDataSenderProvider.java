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
import com.navercorp.pinpoint.profiler.context.module.DefaultClientFactory;
import com.navercorp.pinpoint.profiler.context.module.MetadataDataSender;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.context.thrift.config.ThriftTransportConfig;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.MessageSerializer;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.profiler.sender.ThriftMessageSerializer;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import org.apache.thrift.TBase;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TcpDataSenderProvider implements Provider<EnhancedDataSender<MetaDataType>> {
    private final ThriftTransportConfig thriftTransportConfig;
    private final Provider<PinpointClientFactory> clientFactoryProvider;
    private final Provider<HeaderTBaseSerializer> tBaseSerializerProvider;
    private final MessageConverter<MetaDataType, TBase<?, ?>> messageConverter;

    @Inject
    public TcpDataSenderProvider(ThriftTransportConfig thriftTransportConfig,
                                 @DefaultClientFactory Provider<PinpointClientFactory> clientFactoryProvider,
                                 Provider<HeaderTBaseSerializer> tBaseSerializerProvider,
                                 @MetadataDataSender MessageConverter<MetaDataType, TBase<?, ?>> messageConverter) {
        this.thriftTransportConfig = Objects.requireNonNull(thriftTransportConfig, "thriftTransportConfig");
        this.clientFactoryProvider = Objects.requireNonNull(clientFactoryProvider, "clientFactoryProvider");
        this.tBaseSerializerProvider = Objects.requireNonNull(tBaseSerializerProvider, "tBaseSerializerProvider");
        this.messageConverter = Objects.requireNonNull(messageConverter, "messageConverter");
    }

    @Override
    public EnhancedDataSender<MetaDataType> get() {
        PinpointClientFactory clientFactory = clientFactoryProvider.get();

        String collectorTcpServerIp = thriftTransportConfig.getCollectorTcpServerIp();
        int collectorTcpServerPort = thriftTransportConfig.getCollectorTcpServerPort();
        HeaderTBaseSerializer headerTBaseSerializer = tBaseSerializerProvider.get();
        MessageSerializer<MetaDataType, byte[]> messageSerializer = new ThriftMessageSerializer<>(messageConverter, headerTBaseSerializer);
        return new TcpDataSender<>("Default", collectorTcpServerIp, collectorTcpServerPort, clientFactory, messageSerializer);
    }
}
