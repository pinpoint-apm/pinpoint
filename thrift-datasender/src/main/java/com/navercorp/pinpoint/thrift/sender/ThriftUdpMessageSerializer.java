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

import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TBase;

import java.util.function.BiPredicate;


/**
 * not thread safe
 * @author Woonduk Kang(emeroad)
 */
public class ThriftUdpMessageSerializer<T> implements MessageSerializer<T, ByteMessage> {

    public static final int UDP_MAX_PACKET_LENGTH = 65507;

    private final Logger logger = LogManager.getLogger(this.getClass());

    // Caution. not thread safe
    private final MessageSerializer<T, byte[]> messageSerializer;

    private final BiPredicate<byte[], TBase<?, ?>> filter;

    public ThriftUdpMessageSerializer(MessageConverter<T, TBase<?, ?>> messageConverter, HeaderTBaseSerializer serializer) {
        this.filter = new MaxBytesLengthPredicate<>(logger, ThriftUdpMessageSerializer.UDP_MAX_PACKET_LENGTH);
        this.messageSerializer = new ThriftMessageSerializer<>(messageConverter, serializer, filter);
    }

    public ThriftUdpMessageSerializer(MessageConverter<T, TBase<?, ?>> messageConverter, HeaderTBaseSerializer serializer,
                                      BiPredicate<byte[], TBase<?, ?>> filter) {
        this.filter = filter;
        this.messageSerializer = new ThriftMessageSerializer<>(messageConverter, serializer, filter);
    }

    // single thread only
    @Override
    public ByteMessage serializer(T message) {
        if (message == null) {
            return null;
        }
        byte[] bytes = messageSerializer.serializer(message);
        if (bytes == null) {
            return null;
        }

        return ByteMessage.wrap(bytes);
    }
}
