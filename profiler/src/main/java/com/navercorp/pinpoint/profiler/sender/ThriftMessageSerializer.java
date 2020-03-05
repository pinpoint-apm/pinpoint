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
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.TBaseSerializer;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ThriftMessageSerializer implements MessageSerializer<byte[]> {

    public static final int UDP_MAX_PACKET_LENGTH = 65507;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Caution. not thread safe
    private final TBaseSerializer serializer;
    private final MessageConverter<TBase<?, ?>> messageConverter;

    public ThriftMessageSerializer(MessageConverter<TBase<?, ?>> messageConverter) {
        this(messageConverter, HeaderTBaseSerializerFactory.DEFAULT_FACTORY.createSerializer());
    }

    public ThriftMessageSerializer(MessageConverter<TBase<?, ?>> messageConverter, TBaseSerializer serializer) {
        this.messageConverter = Assert.requireNonNull(messageConverter, "messageConverter");
        this.serializer = Assert.requireNonNull(serializer, "serializer");

    }

    // single thread only
    @Override
    public byte[] serializer(Object message) {
        if (message instanceof TBase<?, ?>) {
            final TBase<?, ?> tBase = (TBase<?, ?>) message;
            return serialize(serializer, tBase);
        }

        final TBase<?, ?> tBase = messageConverter.toMessage(message);
        if (tBase != null) {
            return serialize(serializer, tBase);
        }
        return null;
    }

    private byte[] serialize(TBaseSerializer serializer, TBase tBase) {
        try {
            return serializer.serialize(tBase);
        } catch (TException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Serialize {} failed. Error:{}", tBase, e.getMessage(), e);
            }
        }
        return null;
    }

}
