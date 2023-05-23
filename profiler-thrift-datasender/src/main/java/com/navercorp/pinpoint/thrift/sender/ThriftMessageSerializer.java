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
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.TBaseSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ThriftMessageSerializer<T> implements MessageSerializer<T, byte[]> {

    public static final int UDP_MAX_PACKET_LENGTH = 65507;

    private final Logger logger = LogManager.getLogger(this.getClass());

    // Caution. not thread safe
    private final TBaseSerializer serializer;
    private final MessageConverter<T, TBase<?, ?>> messageConverter;

    private final BiPredicate<byte[], TBase<?, ?>> filter;

    public ThriftMessageSerializer(MessageConverter<T, TBase<?, ?>> messageConverter) {
        this(messageConverter, HeaderTBaseSerializerFactory.DEFAULT_FACTORY.createSerializer());
    }

    public ThriftMessageSerializer(MessageConverter<T, TBase<?, ?>> messageConverter, TBaseSerializer serializer) {
        this.messageConverter = Objects.requireNonNull(messageConverter, "messageConverter");
        this.serializer = Objects.requireNonNull(serializer, "serializer");
        this.filter = null;
    }

    public ThriftMessageSerializer(MessageConverter<T, TBase<?, ?>> messageConverter, TBaseSerializer serializer, BiPredicate<byte[], TBase<?, ?>> filter) {
        this.messageConverter = Objects.requireNonNull(messageConverter, "messageConverter");
        this.serializer = Objects.requireNonNull(serializer, "serializer");
        this.filter = filter;
    }

    // single thread only
    @Override
    public byte[] serializer(T message) {
        if (message == null) {
            return null;
        }
        final TBase<?, ?> tBase = toTBase(message);
        if (tBase == null) {
            return null;
        }
        return serialize(tBase);
    }

    public TBase<?, ?> toTBase(T message) {
        if (message instanceof TBase<?, ?>) {
            return (TBase<?, ?>) message;
        }
        return messageConverter.toMessage(message);
    }

    public byte[] serialize(TBase<?, ?> tBase) {
        try {
            final byte[] bytes = serializer.serialize(tBase);
            if (filter == null) {
                return bytes;
            }
            if (filter.test(bytes, tBase)) {
                return bytes;
            }
            return null;
        } catch (TException e) {
            logger.warn("Serialize {} failed. Error:{}", tBase, e.getMessage(), e);
        }
        return null;
    }

}
