/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.context.module.DefaultClientFactory;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;

import java.net.InetSocketAddress;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TcpDataSenderProvider implements Provider<EnhancedDataSender> {
    private final ProfilerConfig profilerConfig;
    private final Provider<PinpointClientFactory> clientFactoryProvider;

    @Inject
    public TcpDataSenderProvider(ProfilerConfig profilerConfig, @DefaultClientFactory Provider<PinpointClientFactory> clientFactoryProvider) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (clientFactoryProvider == null) {
            throw new NullPointerException("clientFactoryProvider must not be null");
        }

        this.profilerConfig = profilerConfig;
        this.clientFactoryProvider = clientFactoryProvider;

    }

    @Override
    public EnhancedDataSender get() {
        PinpointClientFactory clientFactory = clientFactoryProvider.get();
        InetSocketAddress address = new InetSocketAddress(profilerConfig.getCollectorTcpServerIp(), profilerConfig.getCollectorTcpServerPort());
        return new TcpDataSender("Default", address, clientFactory);
    }
}
