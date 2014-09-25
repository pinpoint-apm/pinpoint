package com.nhn.pinpoint.common.buffer;

/**
 * @author emeroad
 */
public class OffsetFixedBuffer extends FixedBuffer {

    protected final int startOffset;

    public OffsetFixedBuffer(final byte[] buffer, final int offset) {
        if (buffer == null) {
            throw new NullPointerException("buffer must not be null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("negative offset:" + offset);
        }
        if (offset > buffer.length) {
            throw new IllegalArgumentException("offset:" + offset + " > buffer.length:" + buffer.length);
        }
        this.buffer = buffer;
        this.offset = offset;
        this.startOffset = offset;
    }

    @Override
    public byte[] getBuffer() {
        final int bufferSize = offset - startOffset;
        final byte[] copy = new byte[bufferSize];
        System.arraycopy(buffer, startOffset, copy, 0, bufferSize);
        return copy;
    }
}
