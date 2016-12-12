/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.udp;

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.util.PacketUtils;
import com.navercorp.pinpoint.thrift.io.*;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.List;

/**
 * Chunked UDP packet receiver
 * 
 * @author jaehong.kim
 */
public class ChunkedUDPPacketHandlerFactory<T extends DatagramPacket> implements PacketHandlerFactory<T> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DeserializerFactory<ChunkHeaderTBaseDeserializer> deserializerFactory = new ThreadLocalHeaderTBaseDeserializerFactory<>(new ChunkHeaderTBaseDeserializerFactory());

    private final DispatchHandler dispatchHandler;
    private final TBaseFilter filter;

    private final PacketHandler<T> dispatchPacket = new DispatchPacket();

    public ChunkedUDPPacketHandlerFactory(DispatchHandler dispatchHandler, TBaseFilter<T> filter) {
        if (dispatchHandler == null) {
            throw new NullPointerException("dispatchHandler must not be null");
        }
        this.dispatchHandler = dispatchHandler;
        this.filter = filter;
    }

    @Override
    public PacketHandler<T> createPacketHandler() {
        return this.dispatchPacket;
    }

    // stateless
    private class DispatchPacket implements PacketHandler<T> {

        private DispatchPacket() {
        }

        @Override
        public void receive(DatagramSocket localSocket, T packet) {
            final ChunkHeaderTBaseDeserializer deserializer = deserializerFactory.createDeserializer();
            try {
                List<TBase<?, ?>> list = deserializer.deserialize(packet.getData(), packet.getOffset(), packet.getLength());
                if (list == null) {
                    return;
                }

                for (TBase<?, ?> tBase : list) {
                    if (filter.filter(localSocket, tBase, packet.getSocketAddress()) == TBaseFilter.BREAK) {
                        return;
                    }
                    // dispatch signifies business logic execution
                    dispatchHandler.dispatchSendMessage(tBase);
                }
            } catch (TException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("packet serialize error. SendSocketAddress:{} Cause:{}", packet.getSocketAddress(), e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpDatagramPacket(packet));
                }
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Unexpected error. SendSocketAddress:{} Cause:{} ", packet.getSocketAddress(), e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpDatagramPacket(packet));
                }
            }
        }
    }

}
