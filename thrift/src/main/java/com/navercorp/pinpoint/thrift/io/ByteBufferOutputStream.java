package com.navercorp.pinpoint.thrift.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Not Thread Safe.
 *
 * @Author Taejin Koo
 */
public class ByteBufferOutputStream extends OutputStream implements ResettableOutputStream {

    private final ByteBuffer byteBuffer;

    public ByteBufferOutputStream(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    @Override
    public void write(int b) throws IOException {
        checkWriteAvailable(1);
        byteBuffer.put((byte) b);
    }

    @Override
    public void write(byte[] byteArray, int offset, int length) throws IOException {
        checkWriteAvailable(length);
        byteBuffer.put(byteArray, offset, length);
    }

    private void checkWriteAvailable(int size) {
        if (byteBuffer.remaining() >= size) {
            return;
        }

        byteBuffer.limit(byteBuffer.capacity());
        if (byteBuffer.remaining() < size) {
            throw new BufferOverflowException("write failed remaining-size:" + byteBuffer.remaining() + ", input-size:" + size + ".");
        }
    }

    @Override
    public void flush() throws IOException {
        // ignore
    }

    @Override
    public void close() throws IOException {
        // ignore
    }

    public ByteBuffer getByteBuffer() {
        byteBuffer.flip();
        return byteBuffer;
    }

    public void clear() {
        byteBuffer.clear();
    }

    @Override
    public void mark() {
        byteBuffer.mark();
    }

    @Override
    public void resetToMarkIndex() {
        byteBuffer.reset();
    }

}
