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

import com.navercorp.pinpoint.collector.util.PacketUtils;
import com.navercorp.pinpoint.thrift.io.NetworkAvailabilityCheckPacket;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * @author emeroad
 */
public class NetworkAvailabilityCheckPacketFilter implements TBaseFilter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DatagramSocket socket;

    public NetworkAvailabilityCheckPacketFilter() {
        try {
            this.socket = new DatagramSocket();
        } catch (SocketException ex) {
            throw new RuntimeException("socket create fail. error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean filter(TBase<?, ?> tBase, DatagramPacket packet) {
        // Network port availability check packet
        if (tBase instanceof NetworkAvailabilityCheckPacket) {
            if (logger.isInfoEnabled()) {
                logger.info("received udp network availability check packet.");
            }
            responseOK(packet);
            return BREAK;
        }
        return CONTINUE;
    }

    private void responseOK(DatagramPacket packet) {
        try {
            byte[] okBytes = NetworkAvailabilityCheckPacket.DATA_OK;
            DatagramPacket pongPacket = new DatagramPacket(okBytes, okBytes.length, packet.getSocketAddress());
            socket.send(pongPacket);
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
