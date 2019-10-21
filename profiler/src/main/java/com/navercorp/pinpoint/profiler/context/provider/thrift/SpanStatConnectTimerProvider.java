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
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class SpanStatConnectTimerProvider implements Provider<Timer> {

    private final ThriftTransportConfig thriftTransportConfig;

    @Inject
    public SpanStatConnectTimerProvider(ThriftTransportConfig thriftTransportConfig) {
        this.thriftTransportConfig = Assert.requireNonNull(thriftTransportConfig, "thriftTransportConfig");
    }

    @Override
    public Timer get() {
        if ("TCP".equalsIgnoreCase(thriftTransportConfig.getSpanDataSenderTransportType()) || "TCP".equalsIgnoreCase(thriftTransportConfig.getStatDataSenderTransportType())) {
            return createTimer("Pinpoint-SpanStatConnect-Timer");
        }
        return null;
    }

    private static Timer createTimer(String timerName) {
        HashedWheelTimer timer = TimerFactory.createHashedWheelTimer(timerName, 100, TimeUnit.MILLISECONDS, 512);
        timer.start();
        return timer;
    }

}

