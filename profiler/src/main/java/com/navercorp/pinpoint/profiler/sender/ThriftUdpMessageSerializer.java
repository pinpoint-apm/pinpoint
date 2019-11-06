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

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * not thread safe
 * @author Woonduk Kang(emeroad)
 */
public class ThriftUdpMessageSerializer implements MessageSerializer<ByteMessage>{

    public static final int UDP_MAX_PACKET_LENGTH = 65507;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Caution. not thread safe
    private final HeaderTBaseSerializer serializer;
    private final int maxPacketLength;
    private final MessageConverter<TBase<?, ?>> messageConverter;


    public ThriftUdpMessageSerializer(MessageConverter<TBase<?, ?>> messageConverter, int maxPacketLength) {
        this.messageConverter = Assert.requireNonNull(messageConverter, "messageConverter");
        this.maxPacketLength = maxPacketLength;
        // Caution. not thread safe
        SerializerFactory<HeaderTBaseSerializer> headerTBaseSerializerFactory = new HeaderTBaseSerializerFactory(false, maxPacketLength, false);
        serializer = headerTBaseSerializerFactory.createSerializer();
    }

    // single thread only
    @Override
    public ByteMessage serializer(Object message) {
        if (message instanceof TBase<?, ?>) {
            final TBase<?, ?> tBase = (TBase<?, ?>) message;
            return serialize(tBase);
        }

        final TBase<?, ?> tBase = messageConverter.toMessage(message);
        if (tBase != null) {
            return serialize(tBase);
        }
        return null;
    }

    public ByteMessage serialize(TBase<?, ?> message) {
        final TBase<?, ?> dto = message;
        // do not copy bytes because it's single threaded
        final byte[] internalBufferData = serialize(this.serializer, dto);
        if (internalBufferData == null) {
            logger.warn("interBufferData is null");
            return null;
        }

        final int messageSize = this.serializer.getInterBufferSize();
        if (isLimit(messageSize)) {
            // When packet size is greater than UDP packet size limit, it's better to discard packet than let the socket API fails.
            logger.warn("discard packet. Caused:too large message. size:{}, {}", messageSize, dto);
            return null;
        }

        return new ByteMessage(internalBufferData, messageSize);
    }

    private byte[] serialize(HeaderTBaseSerializer serializer, TBase tBase) {
        try {
            return serializer.serialize(tBase);
        } catch (TException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Serialize " + tBase + " failed. Error:" + e.getMessage(), e);
            }
        }
        return null;
    }

    @VisibleForTesting
    protected boolean isLimit(int interBufferSize) {
        if (interBufferSize > maxPacketLength) {
            return true;
        }
        return false;
    }
}
