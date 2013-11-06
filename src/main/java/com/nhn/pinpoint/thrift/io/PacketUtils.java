package com.nhn.pinpoint.thrift.io;

import java.net.DatagramPacket;
import java.util.Arrays;

/**
 * @author emeroad
 */
public class PacketUtils {

    public static byte[] sliceData(DatagramPacket packet, int startOffset) {
        if (packet == null) {
            throw new NullPointerException("packet must not be null");
        }
        int packetLength = packet.getLength();
        int packetOffset = packet.getOffset();
        byte[] source = packet.getData();
        return Arrays.copyOfRange(source, packetOffset + startOffset, packetLength);
    }

    public static byte[] sliceData(byte[] packet, int startOffset, int length) {
        if (packet == null) {
            throw new NullPointerException("packet must not be null");
        }
        return Arrays.copyOfRange(packet, startOffset, length);
    }
}
