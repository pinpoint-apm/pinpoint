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

import com.navercorp.pinpoint.collector.util.PooledObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Objects;

/**
 * @author emeroad
 */
public class Task implements Runnable {
    private final DatagramSocket localSocket;
    private final PacketHandlerFactory<DatagramPacket> packetHandlerFactory;
    private final PooledObject<DatagramPacket> pooledObject;

    public Task(DatagramSocket localSocket, PacketHandlerFactory<DatagramPacket> packetHandlerFactory, PooledObject<DatagramPacket> pooledObject) {
        this.localSocket = Objects.requireNonNull(localSocket, "localSocket");
        this.packetHandlerFactory = Objects.requireNonNull(packetHandlerFactory, "packetHandlerFactory");
        this.pooledObject = Objects.requireNonNull(pooledObject, "pooledObject");
    }

    @Override
    public void run() {
        PacketHandler<DatagramPacket> packetHandler = packetHandlerFactory.createPacketHandler();
        final DatagramPacket packet = pooledObject.getObject();
        try {
            packetHandler.receive(localSocket, packet);
        } finally {
            pooledObject.returnObject();
        }
    }
}
