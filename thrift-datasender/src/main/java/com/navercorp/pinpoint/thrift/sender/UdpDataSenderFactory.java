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

package com.navercorp.pinpoint.thrift.sender;

import com.navercorp.pinpoint.common.profiler.message.DataSender;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TBase;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public final class UdpDataSenderFactory<T> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    //    String host, int port, String threadName, int queueSize, int timeout, int sendBufferSize
    private final String host;
    private final int port;
    private final String threadName;
    private final int queueSize;
    private final int timeout;
    private final int sendBufferSize;
    private final MessageConverter<T, TBase<?, ?>> messageConverter;

    public UdpDataSenderFactory(String host, int port, String threadName, int queueSize, int timeout, int sendBufferSize, MessageConverter<T, TBase<?, ?>> messageConverter) {
        this.host = host;
        this.port = port;
        this.threadName = threadName;
        this.queueSize = queueSize;
        this.timeout = timeout;
        this.sendBufferSize = sendBufferSize;

        this.messageConverter = Objects.requireNonNull(messageConverter, "messageConverter");
    }

    public DataSender<T> create() {
        SerializerFactory<HeaderTBaseSerializer> serializerFactory = new HeaderTBaseSerializerFactory(
                ThriftUdpMessageSerializer.UDP_MAX_PACKET_LENGTH,
                HeaderTBaseSerializerFactory.DEFAULT_TBASE_LOCATOR);
        final MessageSerializer<T, ByteMessage> thriftMessageSerializer = new ThriftUdpMessageSerializer(messageConverter, serializerFactory.createSerializer());
        return new UdpDataSender<>(host, port, threadName, queueSize, timeout, sendBufferSize, thriftMessageSerializer);
    }

}
