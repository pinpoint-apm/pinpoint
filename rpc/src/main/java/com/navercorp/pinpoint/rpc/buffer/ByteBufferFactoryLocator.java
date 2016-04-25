package com.navercorp.pinpoint.rpc.buffer;

import java.util.EnumMap;
import java.util.Map;

/**
 * @Author Taejin Koo
 */
public final class ByteBufferFactoryLocator {

    private static final Map<ByteBufferType, ByteBufferFactory> FACTORY_REPOSITORY = new EnumMap<ByteBufferType, ByteBufferFactory>(ByteBufferType.class);
    static {
        FACTORY_REPOSITORY.put(ByteBufferType.DIRECT, new DirectByteBufferFactory());
        FACTORY_REPOSITORY.put(ByteBufferType.HEAP, new HeapByteBufferFactory());
    }

    public static ByteBufferFactory getFactory(String name) {
        ByteBufferType byteBufferType = ByteBufferType.getValue(name);
        if (byteBufferType == null) {
            throw new IllegalArgumentException("Unknown ByteBufferType:" + name);
        }
        return getFactory(byteBufferType);
    }

    public static ByteBufferFactory getFactory(ByteBufferType byteBufferType) {
        return FACTORY_REPOSITORY.get(byteBufferType);
    }

}
