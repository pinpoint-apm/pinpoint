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

package com.navercorp.pinpoint.collector.receiver.thrift.udp;

import com.navercorp.pinpoint.thrift.io.NetworkAvailabilityCheckPacket;
import org.apache.thrift.TBase;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.*;

/**
 * @author emeroad
 */
public class NetworkAvailabilityCheckPacketFilter<T extends SocketAddress> implements TBaseFilter<T> {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public NetworkAvailabilityCheckPacketFilter() {
    }

    @Override
    public boolean filter(DatagramSocket localSocket, TBase<?, ?> tBase, T remoteHostAddress) {
        // Network port availability check packet
        if (tBase instanceof NetworkAvailabilityCheckPacket) {
            if (logger.isInfoEnabled()) {
                logger.info("received udp network availability check packet. remoteAddress:{}", remoteHostAddress);
            }
            responseOK(localSocket, remoteHostAddress);
            return BREAK;
        }
        return CONTINUE;
    }

    private void responseOK(DatagramSocket socket, T remoteHostAddress) {
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



}
