package com.navercorp.pinpoint.rpc.buffer;

/**
 * @Author Taejin Koo
 */
public enum ByteBufferType {

    HEAP,
    DIRECT;

    public static ByteBufferType getValue(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }

        for (ByteBufferType byteBufferType : ByteBufferType.values()) {
            if (name.equalsIgnoreCase(byteBufferType.name())) {
                return byteBufferType;
            }
        }

        return null;
    }

}
