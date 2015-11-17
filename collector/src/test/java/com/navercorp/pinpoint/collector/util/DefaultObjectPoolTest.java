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

import org.junit.Assert;
import org.junit.Test;

import java.net.DatagramPacket;

/**
 * @author emeroad
 */
public class DefaultObjectPoolTest {

    @Test
    public void testGetObject() throws Exception {
        DefaultObjectPool<DatagramPacket> pool = new DefaultObjectPool<>(new DatagramPacketFactory(), 1);

        PooledObject<DatagramPacket> pooledObject = pool.getObject();
        Assert.assertEquals(0, pool.size());

        pooledObject.returnObject();
        Assert.assertEquals(1, pool.size());
    }

    @Test
    public void testReset() throws Exception {
        DefaultObjectPool<DatagramPacket> pool = new DefaultObjectPool<>(new DatagramPacketFactory(), 1);

        PooledObject<DatagramPacket> pooledObject = pool.getObject();
        DatagramPacket packet = pooledObject.getObject();

        packet.setLength(10);

        pooledObject.returnObject();

        DatagramPacket check = pooledObject.getObject();

        Assert.assertSame(check, packet);
        Assert.assertEquals(packet.getLength(), DatagramPacketFactory.UDP_MAX_PACKET_LENGTH);
    }
}