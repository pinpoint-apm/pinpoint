package com.nhn.pinpoint.common.io;

import java.net.DatagramPacket;
import java.util.Arrays;

public class PacketUtils {

    public static byte[] sliceData(DatagramPacket packet, int startOffset) {
        int packetLength = packet.getLength();
        int packetOffset = packet.getOffset();
        byte[] source = packet.getData();
        return Arrays.copyOfRange(source, packetOffset + startOffset, packetLength);
    }

    public static byte[] sliceData(byte[] packet, int startOffset, int length) {
        return Arrays.copyOfRange(packet, startOffset, length);
    }
}
