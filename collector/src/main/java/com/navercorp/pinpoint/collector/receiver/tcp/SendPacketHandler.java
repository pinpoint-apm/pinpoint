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
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseDeserializerFactory;
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
public class SendPacketHandler implements PinpointPacketHandler<SendPacket> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final DispatchHandler dispatchHandler;

    private final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory;

    public SendPacketHandler(DispatchHandler dispatchHandler) {
        this(dispatchHandler, new ThreadLocalHeaderTBaseDeserializerFactory<>(new HeaderTBaseDeserializerFactory()));
    }

    public SendPacketHandler(DispatchHandler dispatchHandler, DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory) {
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
        this.deserializerFactory = Objects.requireNonNull(deserializerFactory, "deserializerFactory must not be null");
    }

    @Override
    public void handle(SendPacket packet, PinpointSocket pinpointSocket) {
        Objects.requireNonNull(packet, "packet must not be null");
        Objects.requireNonNull(pinpointSocket, "pinpointSocket must not be null");
        
        byte[] payload = packet.getPayload();
        Objects.requireNonNull(payload, "payload must not be null");

        SocketAddress remoteAddress = pinpointSocket.getRemoteAddress();
        try {
            TBase<?, ?> tBase = SerializationUtils.deserialize(payload, deserializerFactory);
            dispatchHandler.dispatchSendMessage(tBase);
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
