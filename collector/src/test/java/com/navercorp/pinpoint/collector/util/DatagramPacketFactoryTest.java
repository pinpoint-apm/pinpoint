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
public class DatagramPacketFactoryTest {

    @Test
    public void testCreate() {
        int bufferLength = 10;
        DatagramPacketFactory factory = new DatagramPacketFactory(bufferLength);
        DatagramPacket packet = factory.create();
        Assertions.assertEquals(bufferLength, packet.getLength());
    }

    @Test
    public void testBeforeReturn() {
        int bufferLength = 10;
        DatagramPacketFactory factory = new DatagramPacketFactory(bufferLength);
        DatagramPacket packet = factory.create();

        packet.setLength(1);
        factory.beforeReturn(packet);
        Assertions.assertEquals(bufferLength, packet.getLength());

    }
}