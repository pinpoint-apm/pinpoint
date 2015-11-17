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

import com.navercorp.pinpoint.thrift.io.NetworkAvailabilityCheckPacket;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.net.*;

/**
 * @author emeroad
 */
public class NetworkAvailabilityCheckPacketFilter<T extends SocketAddress> implements TBaseFilter<T>, DisposableBean {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DatagramSocket socket;

    public NetworkAvailabilityCheckPacketFilter() {
        try {
            this.socket = new DatagramSocket();
            logger.info("port:{}", this.socket.getLocalAddress());
        } catch (SocketException ex) {
            throw new RuntimeException("socket create fail. error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean filter(TBase<?, ?> tBase, T remoteHostAddress) {
        // Network port availability check packet
        if (tBase instanceof NetworkAvailabilityCheckPacket) {
            if (logger.isInfoEnabled()) {
                logger.info("received udp network availability check packet. remoteAddress:{}", remoteHostAddress);
            }
            responseOK(remoteHostAddress);
            return BREAK;
        }
        return CONTINUE;
    }

    private void responseOK(T remoteHostAddress) {
        try {
            byte[] okBytes = NetworkAvailabilityCheckPacket.DATA_OK;
            DatagramPacket pongPacket = new DatagramPacket(okBytes, okBytes.length, remoteHostAddress);
            socket.send(pongPacket);
        } catch (IOException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("pong error. SendSocketAddress:{} Cause:{}", remoteHostAddress, e.getMessage(), e);
            }
        }
    }


    @Override
    public void destroy() throws Exception {
        if (socket!= null) {
            try {
                socket.close();
            } catch (Exception e) {
                logger.warn("socket.close() error:" + e.getMessage(), e);
            }
        }
    }
}
