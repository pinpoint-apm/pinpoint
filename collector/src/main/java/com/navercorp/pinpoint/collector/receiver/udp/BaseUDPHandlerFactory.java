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
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseDeserializerFactory;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.List;

/**
 * @author emeroad
 * @author netspider
 * @author minwoo.jung
 */
public class BaseUDPHandlerFactory<T extends DatagramPacket> implements PacketHandlerFactory<T> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory = new ThreadLocalHeaderTBaseDeserializerFactory<>(new HeaderTBaseDeserializerFactory());

    private final DispatchHandler dispatchHandler;

    private final TBaseFilter<SocketAddress> filter;

    private final PacketHandler<T> dispatchPacket = new DispatchPacket();
    
    private final InetAddress[] ignoreAddresses;

    public BaseUDPHandlerFactory(DispatchHandler dispatchHandler, TBaseFilter<SocketAddress> filter, List<InetAddress> ignoreAddressList) {
        if (dispatchHandler == null) {
            throw new NullPointerException("dispatchHandler must not be null");
        }
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }
        this.dispatchHandler = dispatchHandler;
        this.filter = filter;

        InetAddress[] inetAddressArray = new InetAddress[ignoreAddressList.size()];
        this.ignoreAddresses = ignoreAddressList.toArray(inetAddressArray);
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
            if (isIgnoreAddress(packet.getAddress())) {
                return;
            }
            
            final HeaderTBaseDeserializer deserializer = deserializerFactory.createDeserializer();
            SocketAddress socketAddress = packet.getSocketAddress();
            TBase<?, ?> tBase = null;
            
            try {
                tBase = deserializer.deserialize(packet.getData());
                if (filter.filter(localSocket, tBase, socketAddress) == TBaseFilter.BREAK) {
                    return;
                }
                // dispatch signifies business logic execution
                dispatchHandler.dispatchSendMessage(tBase);
            } catch (TException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("packet serialize error. SendSocketAddress:{} Cause:{}", socketAddress, e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpDatagramPacket(packet));
                }
            } catch (Exception e) {
                // there are cases where invalid headers are received
                if (logger.isWarnEnabled()) {
                    logger.warn("Unexpected error. SendSocketAddress:{} Cause:{} tBase:{}", socketAddress, e.getMessage(), tBase, e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpDatagramPacket(packet));
                }
            }
        }
        
        private boolean isIgnoreAddress(InetAddress remoteAddress) {
            if (ignoreAddresses == null) {
                return false;
            }
            if (remoteAddress == null) {
                return false;
            }
            for (InetAddress ignore : ignoreAddresses) {
                if (ignore.equals(remoteAddress)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("UDP Connected ignore address. IP : " + remoteAddress.getHostAddress());
                    }
                    return true;
                }
            }
            return false;
        }
    }

}
