/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.sender;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import org.apache.thrift.TBase;

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
    private final MessageConverter<TBase<?, ?>> messageConverter;

    public UdpDataSenderFactory(String host, int port, String threadName, int queueSize, int timeout, int sendBufferSize, MessageConverter<TBase<?, ?>> messageConverter) {
        this.host = host;
        this.port = port;
        this.threadName = threadName;
        this.queueSize = queueSize;
        this.timeout = timeout;
        this.sendBufferSize = sendBufferSize;

        this.messageConverter = Assert.requireNonNull(messageConverter, "messageConverter");
    }

    public DataSender create(String typeName) {
        return create(UdpDataSenderType.valueOf(typeName));
    }

    public DataSender create(UdpDataSenderType type) {
        if (type == UdpDataSenderType.NIO) {
            return new NioUDPDataSender(host, port, threadName, queueSize, timeout, sendBufferSize, messageConverter);
        } else if (type == UdpDataSenderType.OIO) {
            final MessageSerializer<ByteMessage> thriftMessageSerializer = new ThriftUdpMessageSerializer(messageConverter, ThriftUdpMessageSerializer.UDP_MAX_PACKET_LENGTH);
            return new UdpDataSender(host, port, threadName, queueSize, timeout, sendBufferSize, thriftMessageSerializer);
        } else {
            throw new IllegalArgumentException("Unknown type.");
        }
    }

}
