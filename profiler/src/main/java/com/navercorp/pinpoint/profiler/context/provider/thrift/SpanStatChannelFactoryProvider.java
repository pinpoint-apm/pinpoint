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
import com.navercorp.pinpoint.profiler.context.module.SpanStatConnectTimer;
import com.navercorp.pinpoint.rpc.client.ClientChannelFactory;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.util.Timer;

/**
 * @author Taejin Koo
 */
public class SpanStatChannelFactoryProvider implements Provider<ChannelFactory> {

    private final ThriftTransportConfig thriftTransportConfig;
    private final Provider<Timer> connectTimerProvider;

    @Inject
    public SpanStatChannelFactoryProvider(ThriftTransportConfig thriftTransportConfig, @SpanStatConnectTimer Provider<Timer> connectTimerProvider) {
        this.thriftTransportConfig = Assert.requireNonNull(thriftTransportConfig, "thriftTransportConfig");
        this.connectTimerProvider = Assert.requireNonNull(connectTimerProvider, "connectTimerProvider");
    }

    @Override
    public ChannelFactory get() {
        int workerCount = 0;

        if ("TCP".equalsIgnoreCase(thriftTransportConfig.getSpanDataSenderTransportType())) {
            workerCount++;
        }
        if ("TCP".equalsIgnoreCase(thriftTransportConfig.getStatDataSenderTransportType())) {
            workerCount++;
        }

        if (workerCount == 0) {
            return null;
        } else {
            Timer timer = connectTimerProvider.get();

            final ClientChannelFactory clientChannelFactory = new ClientChannelFactory();
            return clientChannelFactory.createChannelFactory(1, workerCount, timer);
        }
    }

}
