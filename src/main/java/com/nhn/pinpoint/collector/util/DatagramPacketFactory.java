package com.nhn.pinpoint.collector.util;

import java.net.DatagramPacket;

/**
 *
 */
public class DatagramPacketFactory implements FixedPoolFactory<DatagramPacket> {

    private static final int AcceptedSize = 65507;

    @Override
    public DatagramPacket create() {
        byte[] bytes = new byte[AcceptedSize];
        return new DatagramPacket(bytes, 0, bytes.length);
    }

    @Override
    public void beforeReturn(DatagramPacket packet) {
        packet.setLength(AcceptedSize);
    }
}
