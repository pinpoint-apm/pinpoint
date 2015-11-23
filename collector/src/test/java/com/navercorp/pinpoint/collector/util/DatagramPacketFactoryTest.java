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
public class DatagramPacketFactoryTest {

    @Test
    public void testCreate() throws Exception {
        int bufferLength = 10;
        DatagramPacketFactory factory = new DatagramPacketFactory(bufferLength);
        DatagramPacket packet = factory.create();
        Assert.assertEquals(bufferLength, packet.getLength());
    }

    @Test
    public void testBeforeReturn() throws Exception {
        int bufferLength = 10;
        DatagramPacketFactory factory = new DatagramPacketFactory(bufferLength);
        DatagramPacket packet = factory.create();

        packet.setLength(1);
        factory.beforeReturn(packet);
        Assert.assertEquals(bufferLength, packet.getLength());

    }
}