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

package com.navercorp.pinpoint.collector.receiver.thrift.udp;

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.util.PacketUtils;
import com.navercorp.pinpoint.collector.util.PooledObject;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.io.request.DefaultServerRequest;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseDeserializerFactory;
import org.apache.thrift.TBase;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;

/**
 * @author emeroad
 * @author netspider
 * @author minwoo.jung
 */
public class BaseUDPHandlerFactory<T extends DatagramPacket> implements PacketHandlerFactory<T> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory = new ThreadLocalHeaderTBaseDeserializerFactory<>(new HeaderTBaseDeserializerFactory());

    private final DispatchHandler<TBase<?, ?>, TBase<?, ?>> dispatchHandler;

    private final TBaseFilter<SocketAddress> filter;

    private final PacketHandler<T> dispatchPacket = new DispatchPacket();
    
    private final AddressFilter ignoreAddressFilter;

    public BaseUDPHandlerFactory(DispatchHandler<TBase<?, ?>, TBase<?, ?>> dispatchHandler, TBaseFilter<SocketAddress> filter, AddressFilter ignoreAddressFilter) {
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler");
        this.filter = Objects.requireNonNull(filter, "filter");
        this.ignoreAddressFilter = Objects.requireNonNull(ignoreAddressFilter, "ignoreAddressFilter");
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
        public void receive(DatagramSocket localSocket, PooledObject<T> pooledPacket) {
            final T packet = pooledPacket.getObject();
            final InetSocketAddress remoteSocketAddress = (InetSocketAddress) packet.getSocketAddress();
            final InetAddress remoteAddress = remoteSocketAddress.getAddress();
            if (isIgnoreAddress(remoteAddress)) {
                pooledPacket.returnObject();
                return;
            }

            final Message<TBase<?, ?>> message = deserialize(pooledPacket);
            if (message == null) {
                return;
            }

            try {
                TBase<?, ?> data = message.getData();
                if (filter.filter(localSocket, data, remoteSocketAddress) == TBaseFilter.BREAK) {
                    return;
                }
                ServerRequest<TBase<?, ?>> request = newServerRequest(message, remoteSocketAddress);
                // dispatch signifies business logic execution
                dispatchHandler.dispatchSendMessage(request);
            } catch (Throwable e) {
                // there are cases where invalid headers are received
                if (logger.isWarnEnabled()) {
                    logger.warn("Unexpected error. SendSocketAddress:{} Cause:{} message:{}", remoteAddress, e.getMessage(), message, e);
                }
            }
        }

        private Message<TBase<?, ?>> deserialize(PooledObject<T> pooledPacket) {
            T packet = pooledPacket.getObject();
            try {
                final HeaderTBaseDeserializer deserializer = deserializerFactory.createDeserializer();
                return deserializer.deserialize(packet.getData());
            } catch (Throwable e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Packet deserialize error. SendSocketAddress:{} Cause:{}", packet.getSocketAddress(), e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpDatagramPacket(packet));
                }
                return null;
            } finally {
                pooledPacket.returnObject();
            }
        }
        
        private boolean isIgnoreAddress(InetAddress remoteAddress) {
            if (remoteAddress == null) {
                return false;
            }
            if (!ignoreAddressFilter.accept(remoteAddress)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("UDP Connected ignore address. IP : " + remoteAddress.getHostAddress());
                }
                return true;
            }
            return false;
        }
    }

    private ServerRequest<TBase<?, ?>> newServerRequest(Message<TBase<?, ?>> message, InetSocketAddress remoteSocketAddress) {
        final String remoteAddress = remoteSocketAddress.getAddress().getHostAddress();
        final int remotePort = remoteSocketAddress.getPort();

        return new DefaultServerRequest<>(message, remoteAddress, remotePort);
    }

}
