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
import com.navercorp.pinpoint.bootstrap.config.ThriftTransportConfig;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.module.DefaultClientFactory;
import com.navercorp.pinpoint.profiler.context.module.MetadataConverter;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.MessageSerializer;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.profiler.sender.ThriftMessageSerializer;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import org.apache.thrift.TBase;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TcpDataSenderProvider implements Provider<EnhancedDataSender<Object>> {
    private final ThriftTransportConfig thriftTransportConfig;
    private final Provider<PinpointClientFactory> clientFactoryProvider;
    private final Provider<HeaderTBaseSerializer> tBaseSerializerProvider;
    private final MessageConverter<TBase<?, ?>> messageConverter;

    @Inject
    public TcpDataSenderProvider(ThriftTransportConfig thriftTransportConfig, @DefaultClientFactory Provider<PinpointClientFactory> clientFactoryProvider,
                                 Provider<HeaderTBaseSerializer> tBaseSerializerProvider,
                                 @MetadataConverter MessageConverter<TBase<?, ?>> messageConverter) {
        this.thriftTransportConfig = Assert.requireNonNull(thriftTransportConfig, "thriftTransportConfig");
        this.clientFactoryProvider = Assert.requireNonNull(clientFactoryProvider, "clientFactoryProvider");
        this.tBaseSerializerProvider = Assert.requireNonNull(tBaseSerializerProvider, "tBaseSerializerProvider");
        this.messageConverter = Assert.requireNonNull(messageConverter, "messageConverter");
    }

    @Override
    public EnhancedDataSender<Object> get() {
        PinpointClientFactory clientFactory = clientFactoryProvider.get();

        String collectorTcpServerIp = thriftTransportConfig.getCollectorTcpServerIp();
        int collectorTcpServerPort = thriftTransportConfig.getCollectorTcpServerPort();
        HeaderTBaseSerializer headerTBaseSerializer = tBaseSerializerProvider.get();
        MessageSerializer<byte[]> messageSerializer = new ThriftMessageSerializer(messageConverter, headerTBaseSerializer);
        return new TcpDataSender("Default", collectorTcpServerIp, collectorTcpServerPort, clientFactory, messageSerializer);
    }
}
