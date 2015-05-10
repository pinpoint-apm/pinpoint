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

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TBase;

import com.codahale.metrics.Timer;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.SpanStreamConstants;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseDeserializerFactory;

/**
 * @author Taejin Koo
 */
public class SpanStreamUDPReceiver extends AbstractUDPReceiver {

    private DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory = new ThreadLocalHeaderTBaseDeserializerFactory<HeaderTBaseDeserializer>(new HeaderTBaseDeserializerFactory());

    public SpanStreamUDPReceiver(String receiverName, DispatchHandler dispatchHandler, String bindAddress, int port, int receiverBufferSize,
            int workerThreadSize, int workerThreadQueueSize) {
        super(receiverName, dispatchHandler, bindAddress, port, receiverBufferSize, workerThreadSize, workerThreadQueueSize);
    }

    @Override
    Runnable getPacketDispatcher(AbstractUDPReceiver receiver, DatagramPacket packet) {
        return new DispatchPacket(receiver, packet);
    }

    private class DispatchPacket implements Runnable {
        private final AbstractUDPReceiver receiver;
        private final DatagramPacket packet;

        private DispatchPacket(AbstractUDPReceiver receiver, DatagramPacket packet) {
            if (packet == null) {
                throw new NullPointerException("packet must not be null");
            }
            this.receiver = receiver;
            this.packet = packet;
        }

        @Override
        public void run() {
            Timer.Context time = receiver.getTimer().time();

            final HeaderTBaseDeserializer deserializer = (HeaderTBaseDeserializer) deserializerFactory.createDeserializer();

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
            int chunkSize = 0xffff & requestBuffer.getShort();

            try {
                for (int i = 0; i < chunkSize; i++) {
                    byte[] componentData = getComponentData(requestBuffer, deserializer);
                    if (componentData == null) {
                        logger.warn("Buffer Wrong signature: 0x{} (expected: 0x{})", Integer.toHexString(signature & 0xFF),
                                Integer.toHexString(SpanStreamConstants.Protocol.SPAN_STREAM_SIGNATURE & 0xFF));
                        break;
                    }

                    List<TBase<?, ?>> tbaseList = deserializer.deserializeList(componentData);
                    if (tbaseList == null || tbaseList.size() == 0) {
                        continue;
                    }

                    List<TSpanEvent> spanEventList = getSpanEventList(tbaseList);

                    TBase<?, ?> tBase = tbaseList.get(tbaseList.size() - 1);
                    if (tBase instanceof TSpan) {
                        ((TSpan) tBase).setSpanEventList(spanEventList);
                    } else if (tBase instanceof TSpanChunk) {
                        ((TSpanChunk) tBase).setSpanEventList(spanEventList);
                    }
                    receiver.getDispatchHandler().dispatchRequestMessage(tBase);
                }
            } catch (Exception e) {
                logger.warn("Failed to handle receive packet.", e);
            } finally {
                receiver.getDatagramPacketPool().returnObject(packet);
                // what should we do when an exception is thrown?
                time.stop();
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
        if (tbaseList == null || tbaseList.size() == 0) {
            return new ArrayList<TSpanEvent>(0);
        }

        int spanEventListSize = tbaseList.size() - 1;
        List<TSpanEvent> spanEventList = new ArrayList<TSpanEvent>(spanEventListSize);
        for (int i = 0; i < spanEventListSize; i++) {
            TBase<?, ?> tBase = spanEventList.get(i);
            if (tBase instanceof TSpanEvent) {
                spanEventList.add((TSpanEvent) tBase);
            }
        }

        return spanEventList;
    }

}
