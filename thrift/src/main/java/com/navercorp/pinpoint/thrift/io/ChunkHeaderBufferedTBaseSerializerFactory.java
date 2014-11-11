package com.nhn.pinpoint.thrift.io;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

import java.io.ByteArrayOutputStream;

/**
 * @author jaehong.kim
 */
public final class ChunkHeaderBufferedTBaseSerializerFactory implements SerializerFactory<ChunkHeaderBufferedTBaseSerializer> {

    private static final TBaseLocator DEFAULT_TBASE_LOCATOR = new DefaultTBaseLocator();

    private static final TProtocolFactory DEFAULT_PROTOCOL_FACTORY = new TCompactProtocol.Factory();

    private static final boolean DEFAULT_SAFE_GURANTEED = false;

    private static final int DEFAULT_STREAM_SIZE = 1024 * 8;

    private final boolean safetyGuranteed;
    private final int outputStreamSize;
    private final TProtocolFactory protocolFactory;
    private final TBaseLocator locator;

    public ChunkHeaderBufferedTBaseSerializerFactory() {
        this(DEFAULT_SAFE_GURANTEED, DEFAULT_STREAM_SIZE, DEFAULT_PROTOCOL_FACTORY, DEFAULT_TBASE_LOCATOR);
    }

    public ChunkHeaderBufferedTBaseSerializerFactory(boolean safetyGuranteed, int outputStreamSize, TProtocolFactory protocolFactory, TBaseLocator locator) {
        this.safetyGuranteed = safetyGuranteed;
        this.outputStreamSize = outputStreamSize;
        this.protocolFactory = protocolFactory;
        this.locator = locator;
    }

    public boolean isSafetyGuranteed() {
        return safetyGuranteed;
    }

    public int getOutputStreamSize() {
        return outputStreamSize;
    }

    public TProtocolFactory getProtocolFactory() {
        return protocolFactory;
    }

    public TBaseLocator getLocator() {
        return locator;
    }

    @Override
    public ChunkHeaderBufferedTBaseSerializer createSerializer() {
        ByteArrayOutputStream baos = null;
        if (safetyGuranteed) {
            baos = new ByteArrayOutputStream(outputStreamSize);
        } else {
            baos = new UnsafeByteArrayOutputStream(outputStreamSize);
        }

        return new ChunkHeaderBufferedTBaseSerializer(baos, protocolFactory, locator);
    }
}
