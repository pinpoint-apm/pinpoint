package com.nhn.pinpoint.thrift.io;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

/**
 * @author koo.taejin
 */
public final class ChunkHeaderTBaseDeserializerFactory implements DeserializerFactory<ChunkHeaderTBaseDeserializer> {

    private static final TBaseLocator DEFAULT_TBASE_LOCATOR = new DefaultTBaseLocator();

    private static final TProtocolFactory DEFAULT_PROTOCOL_FACTORY = new TCompactProtocol.Factory();

    public static final ChunkHeaderTBaseDeserializerFactory DEFAULT_FACTORY = new ChunkHeaderTBaseDeserializerFactory();

    private final TProtocolFactory protocolFactory;
    private TBaseLocator locator;

    public ChunkHeaderTBaseDeserializerFactory() {
        this(DEFAULT_PROTOCOL_FACTORY, DEFAULT_TBASE_LOCATOR);
    }

    public TBaseLocator getLocator() {
        return locator;
    }

    public TProtocolFactory getProtocolFactory() {
        return protocolFactory;
    }

    public ChunkHeaderTBaseDeserializerFactory(TProtocolFactory protocolFactory, TBaseLocator locator) {
        if (protocolFactory == null) {
            throw new NullPointerException("protocolFactory must not be null");
        }
        if (locator == null) {
            throw new NullPointerException("locator must not be null");
        }
        this.protocolFactory = protocolFactory;
        this.locator = locator;
    }


    @Override
	public ChunkHeaderTBaseDeserializer createDeserializer() {
        return new ChunkHeaderTBaseDeserializer(protocolFactory, locator);
	}

}
