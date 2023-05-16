/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.thrift;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.ByteSizeUnit;
import com.navercorp.pinpoint.profiler.context.module.SpanStatChannelFactory;
import com.navercorp.pinpoint.profiler.context.thrift.config.ThriftTransportConfig;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.util.Timer;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class StatClientFactoryProvider extends AbstractClientFactoryProvider implements Provider<PinpointClientFactory> {

    private final ThriftTransportConfig thriftTransportConfig;
    private final Provider<ChannelFactory> channelFactoryProvider;
    private final Provider<Timer> connectTimerProvider;

    @Inject
    public StatClientFactoryProvider(ThriftTransportConfig thriftTransportConfig,
                                     @SpanStatChannelFactory Provider<ChannelFactory> channelFactoryProvider,
                                     @SpanStatChannelFactory Provider<Timer> connectTimerProvider) {
        this.thriftTransportConfig = Objects.requireNonNull(thriftTransportConfig, "thriftTransportConfig");
        this.channelFactoryProvider = Objects.requireNonNull(channelFactoryProvider, "channelFactoryProvider");
        this.connectTimerProvider = Objects.requireNonNull(connectTimerProvider, "connectTimerProvider");
    }

    public PinpointClientFactory get() {
        if (!"TCP".equalsIgnoreCase(thriftTransportConfig.getStatDataSenderTransportType())) {
            return null;
        }

        PinpointClientFactory pinpointClientFactory = new DefaultPinpointClientFactory(channelFactoryProvider.get(), connectTimerProvider.get());
        pinpointClientFactory.setWriteTimeoutMillis(1000 * 3);
        pinpointClientFactory.setRequestTimeoutMillis(1000 * 5);

        int writeBufferHighWaterMark = getByteSize(thriftTransportConfig.getStatDataSenderWriteBufferHighWaterMark(), ByteSizeUnit.MEGA_BYTES.toBytesSizeAsInt(16));
        int writeBufferLowWaterMark = getByteSize(thriftTransportConfig.getStatDataSenderWriteBufferLowWaterMark(), ByteSizeUnit.MEGA_BYTES.toBytesSizeAsInt(8));
        if (writeBufferLowWaterMark > writeBufferHighWaterMark) {
            logger.warn("must be writeBufferHighWaterMark({}) >= writeBufferLowWaterMark({})", writeBufferHighWaterMark, writeBufferLowWaterMark);
            writeBufferLowWaterMark = writeBufferHighWaterMark;
        }
        pinpointClientFactory.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        pinpointClientFactory.setWriteBufferLowWaterMark(writeBufferLowWaterMark);

        return pinpointClientFactory;
    }

}