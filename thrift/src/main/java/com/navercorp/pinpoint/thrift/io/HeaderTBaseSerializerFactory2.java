package com.navercorp.pinpoint.thrift.io;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

/**
 * @Author Taejin Koo
 */
public class HeaderTBaseSerializerFactory2 implements SerializerFactory<HeaderTBaseSerializer2> {

    private static final TProtocolFactory DEFAULT_PROTOCOL_FACTORY = new TCompactProtocol.Factory();
    private static final TBaseLocator DEFAULT_TBASE_LOCATOR = new DefaultTBaseLocator();

    private final TProtocolFactory protocolFactory;
    private final TBaseLocator tBaseLocator;

    public HeaderTBaseSerializerFactory2() {
        this(DEFAULT_PROTOCOL_FACTORY, DEFAULT_TBASE_LOCATOR);
    }

    public HeaderTBaseSerializerFactory2(TProtocolFactory protocolFactory) {
        this(protocolFactory, DEFAULT_TBASE_LOCATOR);
    }

    public HeaderTBaseSerializerFactory2(TBaseLocator tBaseLocator) {
        this(DEFAULT_PROTOCOL_FACTORY, tBaseLocator);
    }

    public HeaderTBaseSerializerFactory2(TProtocolFactory protocolFactory, TBaseLocator tBaseLocator) {
        this.protocolFactory = protocolFactory;
        this.tBaseLocator = tBaseLocator;
    }

    @Override
    public HeaderTBaseSerializer2 createSerializer() {
        return new HeaderTBaseSerializer2(protocolFactory, tBaseLocator);
    }

}
