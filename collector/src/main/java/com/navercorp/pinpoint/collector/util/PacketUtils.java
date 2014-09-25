package com.nhn.pinpoint.collector.util;

import org.apache.hadoop.hbase.util.Bytes;

import java.net.DatagramPacket;

/**
 * @author emeroad
 */
public class PacketUtils {

    public static String dumpDatagramPacket(DatagramPacket datagramPacket) {
        if (datagramPacket == null) {
            return "null";
        }
        return Bytes.toStringBinary(datagramPacket.getData(), 0, datagramPacket.getLength());
    }

    public static String dumpByteArray(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }
        return Bytes.toStringBinary(bytes, 0, bytes.length);
    }
}
