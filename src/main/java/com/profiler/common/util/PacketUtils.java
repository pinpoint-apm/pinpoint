package com.profiler.common.util;

import java.net.DatagramPacket;
import java.util.Arrays;

public class PacketUtils {

    public static byte[] sliceData(DatagramPacket packet, int offset) {
        int packetLength = packet.getLength();
        int packetOffset = packet.getOffset();
        byte[] source = packet.getData();
        return Arrays.copyOfRange(source, packetOffset + offset, packetLength);
    }
}
