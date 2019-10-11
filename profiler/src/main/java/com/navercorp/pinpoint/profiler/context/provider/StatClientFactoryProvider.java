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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
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
public class StatClientFactoryProvider extends AbstractClientFactoryProvider implements Provider<PinpointClientFactory> {

    private final ProfilerConfig profilerConfig;
    private final Provider<ChannelFactory> channelFactoryProvider;
    private final Provider<Timer> spanStatConnectTimer;

    @Inject
    public StatClientFactoryProvider(ProfilerConfig profilerConfig, @SpanStatChannelFactory Provider<ChannelFactory> channelFactoryProvider, @SpanStatConnectTimer Provider<Timer> spanStatConnectTimer) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
        this.channelFactoryProvider = Assert.requireNonNull(channelFactoryProvider, "channelFactoryProvider must not be null");
        this.spanStatConnectTimer = Assert.requireNonNull(spanStatConnectTimer, "spanStatConnectTimer must not be null");
    }

    public PinpointClientFactory get() {
        if (!"TCP".equalsIgnoreCase(profilerConfig.getStatDataSenderTransportType())) {
            return null;
        }

        PinpointClientFactory pinpointClientFactory = new DefaultPinpointClientFactory(channelFactoryProvider.get(), spanStatConnectTimer.get());
        pinpointClientFactory.setWriteTimeoutMillis(1000 * 3);
        pinpointClientFactory.setRequestTimeoutMillis(1000 * 5);
        pinpointClientFactory.setConnectTimeout(profilerConfig.getStatDataSenderSocketConnectTimeout());
        pinpointClientFactory.setReconnectDelay(profilerConfig.getStatDataSenderSocketReconnectInterval());

        int writeBufferHighWaterMark = getByteSize(profilerConfig.getStatDataSenderWriteBufferHighWaterMark(), ByteSizeUnit.MEGA_BYTES.toBytesSizeAsInt(16));
        int writeBufferLowWaterMark = getByteSize(profilerConfig.getStatDataSenderWriteBufferLowWaterMark(), ByteSizeUnit.MEGA_BYTES.toBytesSizeAsInt(8));
        if (writeBufferLowWaterMark > writeBufferHighWaterMark) {
            logger.warn("must be writeBufferHighWaterMark({}) >= writeBufferLowWaterMark({})", writeBufferHighWaterMark, writeBufferLowWaterMark);
            writeBufferLowWaterMark = writeBufferHighWaterMark;
        }
        pinpointClientFactory.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        pinpointClientFactory.setWriteBufferLowWaterMark(writeBufferLowWaterMark);

        return pinpointClientFactory;
    }

}