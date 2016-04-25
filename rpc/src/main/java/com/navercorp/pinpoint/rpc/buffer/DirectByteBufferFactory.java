package com.navercorp.pinpoint.rpc.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @Author Taejin Koo
 */
public class DirectByteBufferFactory implements ByteBufferFactory {

    @Override
    public ByteBuffer getBuffer(int capacity) {
        return getBuffer(DEFAULT_BYTE_ORDER, capacity);
    }

    @Override
    public ByteBuffer getBuffer(ByteOrder endianness, int capacity) {
        return ByteBuffer.allocateDirect(capacity).order(endianness);
    }

}
