package com.profiler.server.util;

import java.net.DatagramPacket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 *
 */
public class DatagramPacketPool {

    private final int AcceptedSize = 65507;
    private BlockingQueue<DatagramPacket> queue;

    public DatagramPacketPool() {
        fill(128);
    }

    public DatagramPacketPool(int objectSize) {
        fill(objectSize);
    }

    public void fill(int objectSize) {
        queue = new ArrayBlockingQueue<DatagramPacket>(objectSize);
        for (int i = 0; i < objectSize; i++) {
            DatagramPacket object = createObject();
            queue.add(object);
        }
    }

    public void clear() {
        queue.clear();
    }

    public DatagramPacket createObject() {
        byte[] bytes = new byte[AcceptedSize];
        return new DatagramPacket(bytes, AcceptedSize);
    }

    public DatagramPacket take() {
        return queue.poll();
    }

    public void add(DatagramPacket datagramPacket) {
        queue.add(datagramPacket);
    }
}
