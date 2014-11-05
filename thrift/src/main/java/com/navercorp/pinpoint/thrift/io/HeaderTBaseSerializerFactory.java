package com.nhn.pinpoint.thrift.io;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

import java.io.ByteArrayOutputStream;

/**
 * @author koo.taejin
 */
public final class HeaderTBaseSerializerFactory implements SerializerFactory<HeaderTBaseSerializer> {

    private static final TBaseLocator DEFAULT_TBASE_LOCATOR = new DefaultTBaseLocator();

    private static final TProtocolFactory DEFAULT_PROTOCOL_FACTORY = new TCompactProtocol.Factory();

    private static final boolean DEFAULT_SAFE_GURANTEED = true;

    public static final int DEFAULT_STREAM_SIZE = 1024 * 8;

    public static final int DEFAULT_UDP_STREAM_MAX_SIZE = 1024 * 64;

    public static final HeaderTBaseSerializerFactory DEFAULT_FACTORY = new HeaderTBaseSerializerFactory();

    private final boolean safetyGuranteed;
    private final int outputStreamSize;
    private final TProtocolFactory protocolFactory;
    private final TBaseLocator locator;

    public HeaderTBaseSerializerFactory() {
        this(DEFAULT_SAFE_GURANTEED, DEFAULT_STREAM_SIZE, DEFAULT_PROTOCOL_FACTORY, DEFAULT_TBASE_LOCATOR);
    }

    public HeaderTBaseSerializerFactory(boolean safetyGuranteed) {
        this(safetyGuranteed, DEFAULT_STREAM_SIZE, DEFAULT_PROTOCOL_FACTORY, DEFAULT_TBASE_LOCATOR);
    }

    public HeaderTBaseSerializerFactory(boolean safetyGuranteed, int outputStreamSize) {
        this(safetyGuranteed, outputStreamSize, DEFAULT_PROTOCOL_FACTORY, DEFAULT_TBASE_LOCATOR);
    }

    public HeaderTBaseSerializerFactory(boolean safetyGuranteed, int outputStreamSize, TProtocolFactory protocolFactory, TBaseLocator locator) {
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
    public HeaderTBaseSerializer createSerializer() {
        ByteArrayOutputStream baos = null;
        if (safetyGuranteed) {
            baos = new ByteArrayOutputStream(outputStreamSize);
        } else {
            baos = new UnsafeByteArrayOutputStream(outputStreamSize);
        }

        return new HeaderTBaseSerializer(baos, protocolFactory, locator);
    }

}
