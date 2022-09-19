package com.navercorp.pinpoint.thrift.io;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

public final class ProtocolFactory {

    private static final TProtocolFactory FACTORY = new TCompactProtocol.Factory(1024 * 60, 4096);

    public static TProtocolFactory getFactory() {
        return FACTORY;
    }
}
