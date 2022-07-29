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
import java.util.Objects;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * not thread safe
 * @author Woonduk Kang(emeroad)
 */
public class ThriftUdpMessageSerializer<T> implements MessageSerializer<T, ByteMessage> {

    public static final int UDP_MAX_PACKET_LENGTH = 65507;

    private final Logger logger = LogManager.getLogger(this.getClass());

    // Caution. not thread safe
    private final HeaderTBaseSerializer serializer;
    private final int maxPacketLength;
    private final MessageConverter<T, TBase<?, ?>> messageConverter;


    public ThriftUdpMessageSerializer(MessageConverter<T, TBase<?, ?>> messageConverter, HeaderTBaseSerializer serializer) {
        this.messageConverter = Objects.requireNonNull(messageConverter, "messageConverter");
        // Caution. not thread safe
        this.serializer = Objects.requireNonNull(serializer, "serializer");
        this.maxPacketLength = ThriftUdpMessageSerializer.UDP_MAX_PACKET_LENGTH;
    }

    // single thread only
    @Override
    public ByteMessage serializer(T message) {
        if (message instanceof TBase<?, ?>) {
            return serialize((TBase<?, ?>) message);
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
        final byte[] byteMessage = serialize(this.serializer, dto);
        if (byteMessage == null) {
            return null;
        }
        if (isLimit(byteMessage.length)) {
            // When packet size is greater than UDP packet size limit, it's better to discard packet than let the socket API fails.
            logger.warn("discard packet. Caused:too large message. size:{}, {}", byteMessage.length, dto);
            return null;
        }

        return ByteMessage.wrap(byteMessage);
    }

    private byte[] serialize(HeaderTBaseSerializer serializer, TBase<?, ?> tBase) {
        try {
            return serializer.serialize(tBase);
        } catch (TException e) {
            logger.warn("Serialize {} failed. Error:{}", tBase, e.getMessage(), e);
        }
        return null;
    }

    @VisibleForTesting
    protected boolean isLimit(int interBufferSize) {
        return interBufferSize > maxPacketLength;
    }
}
