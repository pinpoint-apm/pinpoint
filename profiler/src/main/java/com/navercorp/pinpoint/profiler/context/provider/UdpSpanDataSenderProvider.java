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
public class UdpSpanDataSenderProvider implements Provider<DataSender> {

    private static final String threadName = "Pinpoint-UdpSpanDataExecutor";

    private final String ip;
    private final int port;
    private final int writeQueueSize;
    private final int timeout;
    private final int sendBufferSize;
    private final String senderType;

    @Inject
    public UdpSpanDataSenderProvider(ProfilerConfig profilerConfig) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        this.ip = profilerConfig.getCollectorSpanServerIp();
        this.port = profilerConfig.getCollectorSpanServerPort();
        this.writeQueueSize = profilerConfig.getSpanDataSenderWriteQueueSize();
        this.timeout = profilerConfig.getSpanDataSenderSocketTimeout();
        this.sendBufferSize = profilerConfig.getSpanDataSenderSocketSendBufferSize();
        this.senderType = profilerConfig.getSpanDataSenderSocketType();
    }

    public UdpSpanDataSenderProvider(String ip, int port, int writeQueueSize, int timeout, int sendBufferSize, String senderType) {
        this.ip = ip;
        this.port = port;
        this.writeQueueSize = writeQueueSize;
        this.timeout = timeout;
        this.sendBufferSize = sendBufferSize;
        this.senderType = senderType;
    }


    @Override
    public DataSender get() {
        UdpDataSenderFactory factory = new UdpDataSenderFactory(ip, port, threadName, writeQueueSize, timeout, sendBufferSize);
        return factory.create(senderType);
    }


    @Override
    public String toString() {
        return "UdpSpanDataSenderProvider{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", writeQueueSize=" + writeQueueSize +
                ", timeout=" + timeout +
                ", sendBufferSize=" + sendBufferSize +
                ", senderType='" + senderType + '\'' +
                '}';
    }

}
