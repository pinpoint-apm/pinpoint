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

package com.navercorp.pinpoint.collector.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;

/**
 * @author emeroad
 */
public class DefaultObjectPoolTest {

    @Test
    public void testGetObject() {
        DefaultObjectPool<DatagramPacket> pool = new DefaultObjectPool<>(new DatagramPacketFactory(), 1);

        PooledObject<DatagramPacket> pooledObject = pool.getObject();
        Assertions.assertEquals(0, pool.size());

        pooledObject.returnObject();
        Assertions.assertEquals(1, pool.size());
    }

    @Test
    public void testReset() {
        DefaultObjectPool<DatagramPacket> pool = new DefaultObjectPool<>(new DatagramPacketFactory(), 1);

        PooledObject<DatagramPacket> pooledObject = pool.getObject();
        DatagramPacket packet = pooledObject.getObject();

        packet.setLength(10);

        pooledObject.returnObject();

        DatagramPacket check = pooledObject.getObject();

        Assertions.assertSame(check, packet);
        Assertions.assertEquals(packet.getLength(), DatagramPacketFactory.UDP_MAX_PACKET_LENGTH);
    }
}