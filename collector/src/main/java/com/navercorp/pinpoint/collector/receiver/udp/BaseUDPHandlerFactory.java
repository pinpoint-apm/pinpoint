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
import com.navercorp.pinpoint.common.hbase.exception.HBaseAccessDeniedException;
import com.navercorp.pinpoint.thrift.io.*;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.*;

/**
 * @author emeroad
 * @author netspider
 */
public class BaseUDPHandlerFactory<T extends DatagramPacket> implements PacketHandlerFactory<T>, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory = new ThreadLocalHeaderTBaseDeserializerFactory<HeaderTBaseDeserializer>(new HeaderTBaseDeserializerFactory());

    private UDPReceiver receiver;
    private final DispatchHandler dispatchHandler;

    public BaseUDPHandlerFactory(DispatchHandler dispatchHandler) {
        if (dispatchHandler == null) {
            throw new NullPointerException("dispatchHandler must not be null");
        }
        this.dispatchHandler = dispatchHandler;
    }

    public void setReceiver(UDPReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public PacketHandler<T> createPacketHandler() {
        return new DispatchPacket();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.receiver, "receiver must not be null");
    }

    private class DispatchPacket implements PacketHandler<T> {

        private DispatchPacket() {
        }

        @Override
        public void receive(T packet) {
            final HeaderTBaseDeserializer deserializer = deserializerFactory.createDeserializer();
            TBase<?, ?> tBase = null;
            try {
                tBase = deserializer.deserialize(packet.getData());
                if (tBase instanceof L4Packet) {
                    if (logger.isDebugEnabled()) {
                        L4Packet l4Packet = (L4Packet) tBase;
                        logger.debug("udp l4 packet {}", l4Packet.getHeader());
                    }
                    return;
                }
                // Network port availability check packet
                if (tBase instanceof NetworkAvailabilityCheckPacket) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("received udp network availability check packet.");
                    }
                    responseOK(packet);
                    return;
                }
                // dispatch signifies business logic execution
                dispatchHandler.dispatchSendMessage(tBase);
            } catch (HBaseAccessDeniedException e) {
                logger.debug(e.getMessage());
            } catch (TException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("packet serialize error. SendSocketAddress:{} Cause:{}", packet.getSocketAddress(), e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpDatagramPacket(packet));
                }
            } catch (Exception e) {
                // there are cases where invalid headers are received
                if (logger.isWarnEnabled()) {
                    logger.warn("Unexpected error. SendSocketAddress:{} Cause:{} tBase:{}", packet.getSocketAddress(), e.getMessage(), tBase, e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpDatagramPacket(packet));
                }
            }
        }

        private void responseOK(DatagramPacket packet) {
            try {
                byte[] okBytes = NetworkAvailabilityCheckPacket.DATA_OK;
                DatagramPacket pongPacket = new DatagramPacket(okBytes, okBytes.length, packet.getSocketAddress());
                receiver.getSocket().send(pongPacket);
            } catch (IOException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("pong error. SendSocketAddress:{} Cause:{}", packet.getSocketAddress(), e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpDatagramPacket(packet));
                }
            }
        }
    }

}
