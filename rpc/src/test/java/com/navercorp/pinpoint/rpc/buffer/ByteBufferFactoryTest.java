package com.navercorp.pinpoint.rpc.buffer;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Taejin Koo
 */
public class ByteBufferFactoryTest {

    @Test
    public void directByteBufferFactoryTest() throws Exception {
        ByteBufferFactory byteBufferFactory = ByteBufferFactoryLocator.getFactory("direct");
        ByteBuffer buffer = byteBufferFactory.getBuffer(20);

        assertBufferOrder(buffer, ByteBufferFactory.DEFAULT_BYTE_ORDER);
        assertBufferType(buffer, true);
    }

    @Test
    public void heapByteBufferFactoryTest() throws Exception {
        ByteBufferFactory byteBufferFactory = ByteBufferFactoryLocator.getFactory("heap");
        ByteBuffer buffer = byteBufferFactory.getBuffer(20);

        assertBufferOrder(buffer, ByteBufferFactory.DEFAULT_BYTE_ORDER);
        assertBufferType(buffer, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownByteBufferFactoryTest() throws Exception {
        ByteBufferFactory byteBufferFactory = ByteBufferFactoryLocator.getFactory("unknown");

    }

    private void assertBufferOrder(ByteBuffer byteBuffer, ByteOrder order) {
        if (byteBuffer.order() != order) {
            throw new IllegalArgumentException("");
        }
    }

    private void assertBufferType(ByteBuffer byteBuffer, boolean isDirect) {
        if (byteBuffer.isDirect() != isDirect) {
            throw new IllegalArgumentException("");
        }
    }

}
