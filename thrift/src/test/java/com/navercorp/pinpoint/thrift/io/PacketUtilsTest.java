package com.nhn.pinpoint.thrift.io;


import org.junit.Assert;
import org.junit.Test;

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
