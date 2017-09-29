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
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import com.navercorp.pinpoint.thrift.io.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class SpanStreamUDPPacketHandlerFactory<T extends DatagramPacket> implements PacketHandlerFactory<T> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory = new ThreadLocalHeaderTBaseDeserializerFactory<>(new HeaderTBaseDeserializerFactory());
    private final DispatchHandler dispatchHandler;

    @SuppressWarnings("unused")
    private final TBaseFilter<SocketAddress>  filter;
    private final PacketHandler<T> dispatchPacket = new DispatchPacket();

    public SpanStreamUDPPacketHandlerFactory(DispatchHandler dispatchHandler, TBaseFilter<SocketAddress>  filter) {
        if (dispatchHandler == null) {
            throw new NullPointerException("dispatchHandler must not be null");
        }
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
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
        public void receive(DatagramSocket localSocket, DatagramPacket packet) {
            final HeaderTBaseDeserializer deserializer = deserializerFactory.createDeserializer();

            ByteBuffer requestBuffer = ByteBuffer.wrap(packet.getData());
            if (requestBuffer.remaining() < SpanStreamConstants.START_PROTOCOL_BUFFER_SIZE) {
                return;
            }

            byte signature = requestBuffer.get();
            if (signature != SpanStreamConstants.Protocol.SPAN_STREAM_SIGNATURE) {
                logger.warn("Wrong signature: 0x" + Integer.toHexString(signature & 0xFF) + " (expected: 0x"
                        + Integer.toHexString(SpanStreamConstants.Protocol.SPAN_STREAM_SIGNATURE & 0xFF) + ')');
                return;
            }

            byte version = requestBuffer.get();
            int chunkSize = 0xff & requestBuffer.get();
            SocketAddress socketAddress = packet.getSocketAddress();
            
            try {
                for (int i = 0; i < chunkSize; i++) {
                    byte[] componentData = getComponentData(requestBuffer, deserializer);
                    if (componentData == null) {
                        logger.warn("Buffer Wrong signature: 0x{} (expected: 0x{})", Integer.toHexString(signature & 0xFF),
                                Integer.toHexString(SpanStreamConstants.Protocol.SPAN_STREAM_SIGNATURE & 0xFF));
                        break;
                    }

                    List<TBase<?, ?>> tbaseList = deserializer.deserializeList(componentData);
                    if (CollectionUtils.isEmpty(tbaseList)) {
                        continue;
                    }
                    
                    if (tbaseList.size() == 1) {
                        if (filter.filter(localSocket, tbaseList.get(0), socketAddress) == TBaseFilter.BREAK) {
                            continue;
                        }
                    }

                    List<TSpanEvent> spanEventList = getSpanEventList(tbaseList);

                    TBase<?, ?> tBase = tbaseList.get(tbaseList.size() - 1);
                    if (tBase instanceof TSpan) {
                        ((TSpan) tBase).setSpanEventList(spanEventList);
                    } else if (tBase instanceof TSpanChunk) {
                        ((TSpanChunk) tBase).setSpanEventList(spanEventList);
                    }

                    dispatchHandler.dispatchRequestMessage(tBase);
                }
            } catch (Exception e) {
                logger.warn("Failed to handle receive packet.", e);
            }
        }
    }

    private byte[] getComponentData(ByteBuffer buffer, HeaderTBaseDeserializer deserializer) {
        if (buffer.remaining() < 2) {
            logger.warn("Can't available {} fixed buffer.", 2);
            return null;
        }

        int componentSize = 0xffff & buffer.getShort();
        if (buffer.remaining() < componentSize) {
            logger.warn("Can't available {} fixed buffer.", buffer.remaining());
            return null;
        }

        byte[] componentData = new byte[componentSize];
        buffer.get(componentData);

        return componentData;
    }

    private List<TSpanEvent> getSpanEventList(List<TBase<?, ?>> tbaseList) {
        if (CollectionUtils.isEmpty(tbaseList)) {
            return new ArrayList<>(0);
        }

        int spanEventListSize = tbaseList.size() - 1;
        List<TSpanEvent> spanEventList = new ArrayList<>(spanEventListSize);
        for (int i = 0; i < spanEventListSize; i++) {
            TBase<?, ?> tBase = tbaseList.get(i);
            if (tBase instanceof TSpanEvent) {
                spanEventList.add((TSpanEvent) tBase);
            }
        }

        return spanEventList;
    }

}
