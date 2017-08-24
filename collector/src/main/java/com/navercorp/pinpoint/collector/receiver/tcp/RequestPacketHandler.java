/*
 * Copyright 2017 NAVER Corp.
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
 */

package com.navercorp.pinpoint.collector.receiver.tcp;

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.util.PacketUtils;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class RequestPacketHandler implements PinpointPacketHandler<RequestPacket> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final DispatchHandler dispatchHandler;

    private final SerializerFactory<HeaderTBaseSerializer> serializerFactory;
    private final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory;

    public RequestPacketHandler(DispatchHandler dispatchHandler) {
        this(dispatchHandler, new ThreadLocalHeaderTBaseSerializerFactory<>(new HeaderTBaseSerializerFactory(true, HeaderTBaseSerializerFactory.DEFAULT_UDP_STREAM_MAX_SIZE)));
    }

    public RequestPacketHandler(DispatchHandler dispatchHandler, SerializerFactory<HeaderTBaseSerializer> serializerFactory) {
        this(dispatchHandler, serializerFactory, new ThreadLocalHeaderTBaseDeserializerFactory<>(new HeaderTBaseDeserializerFactory()));
    }

    public RequestPacketHandler(DispatchHandler dispatchHandler, DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory) {
        this(dispatchHandler, new ThreadLocalHeaderTBaseSerializerFactory<>(new HeaderTBaseSerializerFactory(true, HeaderTBaseSerializerFactory.DEFAULT_UDP_STREAM_MAX_SIZE)), deserializerFactory);
    }

    public RequestPacketHandler(DispatchHandler dispatchHandler, SerializerFactory<HeaderTBaseSerializer> serializerFactory, DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory) {
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
        this.serializerFactory = Objects.requireNonNull(serializerFactory, "serializerFactory must not be null");
        this.deserializerFactory = Objects.requireNonNull(deserializerFactory, "deserializerFactory must not be null");
    }

    @Override
    public void handle(RequestPacket packet, PinpointSocket pinpointSocket) {
        Objects.requireNonNull(packet, "packet must not be null");
        Objects.requireNonNull(pinpointSocket, "pinpointSocket must not be null");
        
        byte[] payload = packet.getPayload();
        Objects.requireNonNull(payload, "payload must not be null");

        SocketAddress remoteAddress = pinpointSocket.getRemoteAddress();
        try {
            TBase<?, ?> tBase = SerializationUtils.deserialize(payload, deserializerFactory);
            TBase result = dispatchHandler.dispatchRequestMessage(tBase);
            if (result != null) {
                byte[] resultBytes = SerializationUtils.serialize(result, serializerFactory);
                pinpointSocket.response(packet, resultBytes);
            }
        } catch (TException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("packet serialize error. remote:{} cause:{}", remoteAddress, e.getMessage(), e);
            }
            if (isDebug) {
                logger.debug("packet dump hex:{}", PacketUtils.dumpByteArray(payload));
            }
        } catch (Exception e) {
            // there are cases where invalid headers are received
            if (logger.isWarnEnabled()) {
                logger.warn("Unexpected error. remote:{} cause:{}", remoteAddress, e.getMessage(), e);
            }
            if (isDebug) {
                logger.debug("packet dump hex:{}", PacketUtils.dumpByteArray(payload));
            }
        }
    }

}
