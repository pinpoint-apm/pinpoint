package com.nhn.pinpoint.thrift.io;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

/**
 * @author koo.taejin
 */
public final class HeaderTBaseDeserializerFactory implements DeserializerFactory<HeaderTBaseDeserializer> {

    private static final TBaseLocator DEFAULT_TBASE_LOCATOR = new DefaultTBaseLocator();

    private static final TProtocolFactory DEFAULT_PROTOCOL_FACTORY = new TCompactProtocol.Factory();

    public static final HeaderTBaseDeserializerFactory DEFAULT_FACTORY = new HeaderTBaseDeserializerFactory();

    private final TProtocolFactory protocolFactory;
    private TBaseLocator locator;

    public HeaderTBaseDeserializerFactory() {
        this(DEFAULT_PROTOCOL_FACTORY, DEFAULT_TBASE_LOCATOR);
    }

    public TBaseLocator getLocator() {
        return locator;
    }

    public TProtocolFactory getProtocolFactory() {
        return protocolFactory;
    }

    public HeaderTBaseDeserializerFactory(TProtocolFactory protocolFactory, TBaseLocator locator) {
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
	public HeaderTBaseDeserializer createDeserializer() {
        return new HeaderTBaseDeserializer(protocolFactory, locator);
	}

}
