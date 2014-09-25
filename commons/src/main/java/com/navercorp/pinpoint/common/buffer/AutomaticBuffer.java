package com.nhn.pinpoint.common.buffer;

import com.nhn.pinpoint.common.util.BytesUtils;

/**
 * 버퍼사이즈가 자동으로 확장되는 buffer
 * @author emeroad
 */
public class AutomaticBuffer extends FixedBuffer {

    public AutomaticBuffer() {
        super(32);
    }

    public AutomaticBuffer(final int size) {
        super(size);
    }

    public AutomaticBuffer(final byte[] buffer) {
        super(buffer);
    }


    private void checkExpend(final int size) {
        int length = buffer.length;
        final int remain = length - offset;
        if (remain >= size) {
            return;
        }

        if (length == 0) {
            length = 1;
        }

        // 사이즈 계산을 먼저한 후에 buffer를 한번만 할당하도록 변경.
        final int expendBufferSize = computeExpendBufferSize(size, length, remain);
        // allocate buffer
        final byte[] expendBuffer = new byte[expendBufferSize];
        System.arraycopy(buffer, 0, expendBuffer, 0, buffer.length);
        buffer = expendBuffer;
    }

    private int computeExpendBufferSize(final int size, int length, int remain) {
        int expendBufferSize = 0;
        while (remain < size) {
            length <<= 2;
            expendBufferSize = length;
            remain = expendBufferSize - offset;
        }
        return expendBufferSize;
    }

    @Override
    public void putPadBytes(byte[] bytes, int totalLength) {
        checkExpend(totalLength);
        super.putPadBytes(bytes, totalLength);
    }


    @Override
    public void putPrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            checkExpend(1);
            super.putSVar(NULL);
        } else {
            checkExpend(bytes.length + BytesUtils.VINT_MAX_SIZE);
            super.putSVar(bytes.length);
            super.put(bytes);
        }
    }

    @Override
    public void put2PrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            checkExpend(BytesUtils.SHORT_BYTE_LENGTH);
            super.put((short)NULL);
        } else {
            if (bytes.length > Short.MAX_VALUE) {
                throw new IllegalArgumentException("too large bytes length:" + bytes.length);
            }
            checkExpend(bytes.length + BytesUtils.SHORT_BYTE_LENGTH);
            super.put((short)bytes.length);
            super.put(bytes);
        }
    }

    @Override
    public void put4PrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            checkExpend(BytesUtils.INT_BYTE_LENGTH);
            super.put(NULL);
        } else {
            checkExpend(bytes.length + BytesUtils.INT_BYTE_LENGTH);
            super.put(bytes.length);
            super.put(bytes);
        }
    }

    @Override
    public void putPadString(String string, int totalLength) {
        checkExpend(totalLength);
        super.putPadString(string, totalLength);
    }


    @Override
    public void putPrefixedString(final String string) {
        byte[] bytes = BytesUtils.toBytes(string);
        this.putPrefixedBytes(bytes);
    }

    @Override
    public void put2PrefixedString(final String string) {
        byte[] bytes = BytesUtils.toBytes(string);
        this.put2PrefixedBytes(bytes);
    }

    @Override
    public void put4PrefixedString(final String string) {
        byte[] bytes = BytesUtils.toBytes(string);
        this.put4PrefixedBytes(bytes);
    }

    @Override
    public void put(final byte v) {
        checkExpend(1);
        super.put(v);
    }

    @Override
    public void put(final boolean v) {
        checkExpend(1);
        super.put(v);
    }

    @Override
    public void put(final short v) {
        checkExpend(2);
        super.put(v);
    }

    @Override
    public void put(final int v) {
        checkExpend(4);
        super.put(v);
    }

    public void putVar(final int v) {
        checkExpend(BytesUtils.VLONG_MAX_SIZE);
        super.putVar(v);
    }

    public void putSVar(final int v) {
        checkExpend(BytesUtils.VINT_MAX_SIZE);
        super.putSVar(v);
    }

    public void putVar(final long v) {
        checkExpend(BytesUtils.VLONG_MAX_SIZE);
        super.putVar(v);
    }

    public void putSVar(final long v) {
        checkExpend(BytesUtils.VLONG_MAX_SIZE);
        super.putSVar(v);
    }

    @Override
    public void put(final long v) {
        checkExpend(8);
        super.put(v);
    }

    @Override
    public void put(final byte[] v) {
        if (v == null) {
            throw new NullPointerException("v must not be null");
        }
        checkExpend(v.length);
        super.put(v);
    }
}
