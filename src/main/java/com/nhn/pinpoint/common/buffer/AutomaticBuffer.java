package com.nhn.pinpoint.common.buffer;

/**
 * 버퍼사이즈가 자동으로 확장되는 buffer
 */
public class AutomaticBuffer extends FixedBuffer {

    public AutomaticBuffer() {
        super(32);
    }

    public AutomaticBuffer(int size) {
        super(size);
    }

    public AutomaticBuffer(byte[] buffer) {
        super(buffer);
    }

    public AutomaticBuffer(byte[] buffer, int offset) {
        super(buffer, offset);
    }

    private void checkExpend(int size) {
        int length = buffer.length;
        int remain = length - offset;
        if (remain >= size) {
            return;
        }

        if (length == 0) {
            length = 1;
        }

        // 사이즈 계산을 먼저한 후에 buffer를 한번만 할당하도록 변경.
        int expendBufferSize = computeExpendBufferSize(size, length, remain);
        // allocate buffer
        final byte[] expendBuffer = new byte[expendBufferSize];
        System.arraycopy(buffer, 0, expendBuffer, 0, buffer.length);
        buffer = expendBuffer;
    }

    private int computeExpendBufferSize(int size, int length, int remain) {
        int expendBufferSize = 0;
        while (remain < size) {
            length <<= 2;
            expendBufferSize = length;
            remain = expendBufferSize - offset;
        }
        return expendBufferSize;
    }

    @Override
    public void put1PrefixedBytes(byte[] bytes) {
        if (bytes == null) {
            checkExpend(1);
        } else {
            checkExpend(bytes.length + 1);
        }
        super.put1PrefixedBytes(bytes);
    }

    @Override
    public void put2PrefixedBytes(byte[] bytes) {
        if (bytes == null) {
            checkExpend(2);
        } else {
            checkExpend(bytes.length + 2);
        }
        super.put2PrefixedBytes(bytes);
    }

    @Override
    public void putPrefixedBytes(byte[] bytes) {
        if (bytes == null) {
            checkExpend(4);
        } else {
            checkExpend(bytes.length + 4);
        }
        super.putPrefixedBytes(bytes);
    }

    @Override
    public void putNullTerminatedBytes(byte[] bytes) {
        if (bytes == null) {
            checkExpend(1);
        } else {
            checkExpend(bytes.length + 1);
        }
        super.putNullTerminatedBytes(bytes);
    }

    @Override
    public void put(byte v) {
        checkExpend(1);
        super.put(v);
    }

    @Override
    public void put(boolean v) {
        checkExpend(1);
        super.put(v);
    }

    @Override
    public void put(short v) {
        checkExpend(2);
        super.put(v);
    }

    @Override
    public void put(int v) {
        checkExpend(4);
        super.put(v);
    }

    public void putVar(int v) {
        checkExpend(10);
        super.putVar(v);
    }

    public void putSVar(int v) {
        checkExpend(5);
        super.putSVar(v);
    }

    public void putVar(long v) {
        checkExpend(10);
        super.putVar(v);
    }

    public void putSVar(long v) {
        checkExpend(10);
        super.putSVar(v);
    }

    @Override
    public void put(long v) {
        checkExpend(8);
        super.put(v);
    }

    @Override
    public void put(byte[] v) {
        if (v == null) {
            throw new NullPointerException("v must not be null");
        }
        super.put(v);
    }


}
