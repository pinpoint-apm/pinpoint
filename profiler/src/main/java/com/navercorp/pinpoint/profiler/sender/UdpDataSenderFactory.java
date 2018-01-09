/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.profiler.sender;

/**
 * @author Taejin Koo
 */
public final class UdpDataSenderFactory {

//    String host, int port, String threadName, int queueSize, int timeout, int sendBufferSize

    private final String host;
    private final int port;
    private final String threadName;
    private final int queueSize;
    private final int timeout;
    private final int sendBufferSize;

    public UdpDataSenderFactory(String host, int port, String threadName, int queueSize, int timeout, int sendBufferSize) {
        this.host = host;
        this.port = port;
        this.threadName = threadName;
        this.queueSize = queueSize;
        this.timeout = timeout;
        this.sendBufferSize = sendBufferSize;
    }

    public DataSender create(String typeName) {
        return create(UdpDataSenderType.valueOf(typeName));
    }

    public DataSender create(UdpDataSenderType type) {
        if (type == UdpDataSenderType.NIO) {
            return new NioUDPDataSender(host, port, threadName, queueSize, timeout, sendBufferSize);
        } else if (type == UdpDataSenderType.OIO) {
            return new UdpDataSender(host, port, threadName, queueSize, timeout, sendBufferSize);
        } else {
            throw new IllegalArgumentException("Unknown type.");
        }
    }

}
