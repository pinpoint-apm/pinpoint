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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

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

    private TBaseFilter filter = TBaseFilter.CONTINUE_FILTER;

    public BaseUDPHandlerFactory(DispatchHandler dispatchHandler) {
        if (dispatchHandler == null) {
            throw new NullPointerException("dispatchHandler must not be null");
        }
        this.dispatchHandler = dispatchHandler;
    }

    public void setReceiver(UDPReceiver receiver) {
        this.receiver = receiver;
    }

    public void setFilter(TBaseFilter filter) {
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }
        this.filter = filter;
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
                if (filter.filter(tBase, packet) == TBaseFilter.BREAK) {
                    return;
                }
                // dispatch signifies business logic execution
                dispatchHandler.dispatchSendMessage(tBase);
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
    }

}
