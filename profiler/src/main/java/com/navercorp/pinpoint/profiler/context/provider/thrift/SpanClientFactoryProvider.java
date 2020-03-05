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
import com.navercorp.pinpoint.bootstrap.config.ThriftTransportConfig;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.ByteSizeUnit;
import com.navercorp.pinpoint.profiler.context.module.SpanStatChannelFactory;
import com.navercorp.pinpoint.profiler.context.module.SpanStatConnectTimer;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.util.Timer;

/**
 * @author Taejin Koo
 */
public class SpanClientFactoryProvider extends AbstractClientFactoryProvider implements Provider<PinpointClientFactory> {

    private final ThriftTransportConfig thriftTransportConfig;
    private final Provider<ChannelFactory> channelFactoryProvider;
    private final Provider<Timer> spanStatConnectTimer;

    @Inject
    public SpanClientFactoryProvider(ThriftTransportConfig thriftTransportConfig, @SpanStatChannelFactory Provider<ChannelFactory> channelFactoryProvider, @SpanStatConnectTimer Provider<Timer> spanStatConnectTimer) {
        this.thriftTransportConfig = Assert.requireNonNull(thriftTransportConfig, "thriftTransportConfig");
        this.channelFactoryProvider = Assert.requireNonNull(channelFactoryProvider, "channelFactoryProvider");
        this.spanStatConnectTimer = Assert.requireNonNull(spanStatConnectTimer, "spanStatConnectTimer");
    }

    public PinpointClientFactory get() {
        if (!"TCP".equalsIgnoreCase(thriftTransportConfig.getSpanDataSenderTransportType())) {
            return null;
        }

        PinpointClientFactory pinpointClientFactory = new DefaultPinpointClientFactory(channelFactoryProvider.get(), spanStatConnectTimer.get());
        pinpointClientFactory.setWriteTimeoutMillis(1000 * 3);
        pinpointClientFactory.setRequestTimeoutMillis(1000 * 5);

        int writeBufferHighWaterMark = getByteSize(thriftTransportConfig.getSpanDataSenderWriteBufferHighWaterMark(), ByteSizeUnit.MEGA_BYTES.toBytesSizeAsInt(16));
        int writeBufferLowWaterMark = getByteSize(thriftTransportConfig.getSpanDataSenderWriteBufferLowWaterMark(), ByteSizeUnit.MEGA_BYTES.toBytesSizeAsInt(8));
        if (writeBufferLowWaterMark > writeBufferHighWaterMark) {
            logger.warn("must be writeBufferHighWaterMark({}) >= writeBufferLowWaterMark({})", writeBufferHighWaterMark, writeBufferLowWaterMark);
            writeBufferLowWaterMark = writeBufferHighWaterMark;
        }
        pinpointClientFactory.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        pinpointClientFactory.setWriteBufferLowWaterMark(writeBufferLowWaterMark);

        return pinpointClientFactory;
    }

}