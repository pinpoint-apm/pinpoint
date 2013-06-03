package com.nhn.pinpoint.collector.util;

import org.apache.hadoop.hbase.util.Bytes;

import java.net.DatagramPacket;

/**
 *
 */
public class PacketUtils {

    public static String dumpDatagramPacket(DatagramPacket datagramPacket) {
        if (datagramPacket == null) {
            return "null";
        }
        return Bytes.toStringBinary(datagramPacket.getData(), 0, datagramPacket.getLength());
    }
}
