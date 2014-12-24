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

package com.navercorp.pinpoint.thrift.io;


import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.thrift.io.PacketUtils;

import java.net.DatagramPacket;
import java.util.Arrays;

/**
 * @author emeroad
 */
public class PacketUtilsTest {
    @Test
    public void testSliceData1() throws Exception {
        DatagramPacket packet = createPacket(10);
        packet.setLength(5);


        byte[] bytes1 = PacketUtils.sliceData(packet, 0);
        Assert.assertEquals(bytes1.length, 5);
    }

    @Test
    public void testSliceData2() throws Exception {
        DatagramPacket packet = createPacket(10);
        Arrays.fill(packet.getData(), 1, 8, (byte)'a');

        byte[] bytes1 = PacketUtils.sliceData(packet, 0);
        Assert.assertArrayEquals(bytes1, packet.getData());
    }


    @Test
    public void testSliceData3() throws Exception {
        DatagramPacket packet = createPacket(10);
        Arrays.fill(packet.getData(), 1, 8, (byte)'a');
        packet.setLength(4);

        byte[] bytes1 = PacketUtils.sliceData(packet, 0);
        Assert.assertArrayEquals(bytes1, Arrays.copyOf(packet.getData(), 4));
    }

    private DatagramPacket createPacket(int size) {
        byte[] bytes = new byte[size];
        return new DatagramPacket(bytes, 0, bytes.length);
    }
}
