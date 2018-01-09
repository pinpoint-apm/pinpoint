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

import com.navercorp.pinpoint.collector.util.PooledObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @author emeroad
 */
public class PooledPacketWrap implements Runnable {
    private final DatagramSocket localSocket;
    private final PacketHandler<DatagramPacket> packetHandler;
    private final PooledObject<DatagramPacket> pooledObject;

    public PooledPacketWrap(DatagramSocket localSocket, PacketHandler<DatagramPacket> packetHandler, PooledObject<DatagramPacket> pooledObject) {
        if (localSocket == null) {
            throw new NullPointerException("localSocket must not be null");
        }
        if (packetHandler == null) {
            throw new NullPointerException("packetReceiveHandler must not be null");
        }
        if (pooledObject == null) {
            throw new NullPointerException("pooledObject must not be null");
        }
        this.localSocket = localSocket;
        this.packetHandler = packetHandler;
        this.pooledObject = pooledObject;
    }

    @Override
    public void run() {
        final DatagramPacket packet = pooledObject.getObject();
        try {
            packetHandler.receive(localSocket, packet);
        } finally {
            pooledObject.returnObject();
        }
    }
}
