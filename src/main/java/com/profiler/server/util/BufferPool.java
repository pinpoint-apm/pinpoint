package com.profiler.server.util;

import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 */
public class BufferPool {
    private static final int AcceptedSize = 65507;

    private LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();

    public BufferPool(int size) {

        for (int i = 0; i < size; i++) {
            byte[] bytes = new byte[AcceptedSize];
            queue.offer(bytes);
        }
    }

    public byte[] getBuffer() {
        return queue.poll();
    }

    public void returnPacket(byte[] buffer) {
        queue.offer(buffer);
    }

}
