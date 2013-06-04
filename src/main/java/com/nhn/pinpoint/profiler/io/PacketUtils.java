package com.nhn.pinpoint.profiler.io;

import java.net.DatagramPacket;
import java.util.Arrays;

public class PacketUtils {

    public static byte[] sliceData(DatagramPacket packet, int startOffset) {
        int packetLength = packet.getLength();
        int packetOffset = packet.getOffset();
        byte[] source = packet.getData();
        return Arrays.copyOfRange(source, packetOffset + startOffset, packetLength);
    }
}
