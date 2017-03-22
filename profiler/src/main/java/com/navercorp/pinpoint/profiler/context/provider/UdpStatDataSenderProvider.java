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
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.UdpDataSenderFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class UdpStatDataSenderProvider implements Provider<DataSender> {

    private static final String threadName = "Pinpoint-UdpStatDataExecutor";

    private final String ip;
    private final int port;
    private final int writeQueueSize;
    private final int timeout;
    private final int sendBufferSize;
    private final String senderType;

    @Inject
    public UdpStatDataSenderProvider(ProfilerConfig profilerConfig) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        this.ip = profilerConfig.getCollectorStatServerIp();
        this.port = profilerConfig.getCollectorStatServerPort();
        this.writeQueueSize = profilerConfig.getStatDataSenderWriteQueueSize();
        this.timeout = profilerConfig.getStatDataSenderSocketTimeout();
        this.sendBufferSize = profilerConfig.getStatDataSenderSocketSendBufferSize();
        this.senderType = profilerConfig.getStatDataSenderSocketType();
    }



    @Override
    public DataSender get() {
        UdpDataSenderFactory factory = new UdpDataSenderFactory(ip, port, threadName, writeQueueSize, timeout, sendBufferSize);
        return factory.create(senderType);
    }

    @Override
    public String toString() {
        return "UdpStatDataSenderProvider{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", writeQueueSize=" + writeQueueSize +
                ", timeout=" + timeout +
                ", sendBufferSize=" + sendBufferSize +
                ", senderType='" + senderType + '\'' +
                '}';
    }
}
